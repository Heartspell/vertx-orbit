package com.heartspell.vertxorbit.lifecycle

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class VertxLifecycleAnalyzer {
    private val builtInVerticleTypes = setOf(
        "AbstractVerticle",
        "CoroutineVerticle",
        "io.vertx.core.AbstractVerticle",
        "io.vertx.kotlin.coroutines.CoroutineVerticle"
    )

    fun analyze(file: KtFile): List<LifecycleFinding> {
        return analyzeVerticles(file).flatMap { it.findings }
    }

    fun analyzeVerticles(file: KtFile, knownVerticleTypes: Set<String> = builtInVerticleTypes): List<VerticleAnalysis> {
        val verticles = mutableListOf<VerticleAnalysis>()

        file.accept(object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                if (klass.isVertxVerticle(knownVerticleTypes)) {
                    val findings = mutableListOf<LifecycleFinding>()
                    analyzeVerticleClass(klass, findings)
                    verticles += VerticleAnalysis(
                        className = klass.name ?: "UnnamedVerticle",
                        element = klass.nameIdentifier ?: klass,
                        findings = findings
                    )
                }
                super.visitClass(klass)
            }
        })

        return verticles
    }

    private fun analyzeVerticleClass(klass: KtClass, findings: MutableList<LifecycleFinding>) {
        val className = klass.name ?: "UnnamedVerticle"
        val functions = klass.declarations.filterIsInstance<KtNamedFunction>()
        val startFunctions = functions.filter { it.name == "start" }
        val stopFunctions = functions.filter { it.name == "stop" }

        startFunctions.forEach { function ->
            analyzePromiseCompletion(className, LifecyclePhase.START, function, "startPromise", findings)
        }

        stopFunctions.forEach { function ->
            analyzePromiseCompletion(className, LifecyclePhase.STOP, function, "stopPromise", findings)
        }

        analyzeCoroutineScopes(className, klass, stopFunctions, findings)
        analyzeManagedVertxResources(className, klass, findings)
        analyzeBlockingCalls(className, klass, findings)
        analyzeEventBusRequests(className, klass, findings)
    }

    private fun analyzePromiseCompletion(
        className: String,
        phase: LifecyclePhase,
        function: KtNamedFunction,
        defaultName: String,
        findings: MutableList<LifecycleFinding>
    ) {
        val promiseName = function.valueParameters.firstOrNull()?.name ?: return
        val promiseType = function.valueParameters.firstOrNull()?.typeReference?.text.orEmpty()
        if (
            !promiseName.contains("Promise", ignoreCase = true) &&
            !promiseType.contains("Promise") &&
            promiseName != defaultName
        ) {
            return
        }

        val bodyText = function.bodyExpression?.text.orEmpty()
        val completesPromise = listOf(
            "$promiseName.complete(",
            "$promiseName.fail(",
            "$promiseName.handle(",
            ".onComplete($promiseName)",
            ".onSuccess { $promiseName.complete",
            ".onFailure { $promiseName.fail"
        ).any { bodyText.contains(it) }

        if (!completesPromise) {
            findings += LifecycleFinding(
                className = className,
                phase = phase,
                severity = LifecycleSeverity.WARNING,
                message = "${phase.title.lowercase()} promise may never complete",
                detail = "Complete or fail `$promiseName` on every asynchronous ${phase.title.lowercase()} path.",
                element = function.nameIdentifier ?: function
            )
        }
    }

    private fun analyzeCoroutineScopes(
        className: String,
        klass: KtClass,
        stopFunctions: List<KtNamedFunction>,
        findings: MutableList<LifecycleFinding>
    ) {
        val stopText = stopFunctions.joinToString("\n") { it.bodyExpression?.text.orEmpty() }
        val properties = klass.declarations.filterIsInstance<KtProperty>()

        properties.forEach { property ->
            val name = property.name ?: return@forEach
            val initializerText = property.initializer?.text.orEmpty()
            if (!initializerText.contains("CoroutineScope(")) {
                return@forEach
            }

            val classText = klass.text
            val launchesWork = classText.contains("$name.launch")
            val cancelsScope = stopText.contains("$name.cancel(") || stopText.contains("$name.coroutineContext.cancel")

            if (launchesWork && !cancelsScope) {
                findings += LifecycleFinding(
                    className = className,
                    phase = LifecyclePhase.STOP,
                    severity = LifecycleSeverity.WARNING,
                    message = "Coroutine scope is not cancelled",
                    detail = "Cancel `$name` from `stop()` so coroutine work follows the verticle lifecycle.",
                    element = property.nameIdentifier ?: property
                )
            }
        }
    }

    private fun analyzeManagedVertxResources(
        className: String,
        klass: KtClass,
        findings: MutableList<LifecycleFinding>
    ) {
        klass.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                val callText = expression.qualifiedText()
                val callee = expression.calleeExpression?.text

                when {
                    callee == "consumer" && callText.contains("eventBus") -> {
                        findings += managedFinding(
                            className = className,
                            phase = LifecyclePhase.RUNNING,
                            expression = expression,
                            message = "Event bus consumer: ${expression.firstArgumentLabel()}",
                            detail = "Vert.x automatically unregisters event bus handlers created inside a verticle on undeploy."
                        )
                    }

                    callee == "setTimer" || callee == "setPeriodic" -> {
                        findings += managedFinding(
                            className = className,
                            phase = LifecyclePhase.RUNNING,
                            expression = expression,
                            message = "${callee.removePrefix("set").lowercase()} timer",
                            detail = "Vert.x automatically cancels timers created inside a verticle on undeploy."
                        )
                    }

                    callee == "createHttpServer" -> {
                        findings += managedFinding(
                            className = className,
                            phase = LifecyclePhase.START,
                            expression = expression,
                            message = "HTTP server",
                            detail = "Vert.x automatically closes HTTP servers created inside a verticle on undeploy."
                        )
                    }
                }

                super.visitCallExpression(expression)
            }
        })
    }

    private fun analyzeBlockingCalls(
        className: String,
        klass: KtClass,
        findings: MutableList<LifecycleFinding>
    ) {
        klass.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                val callee = expression.calleeExpression?.text
                val qualifiedText = expression.qualifiedText()
                val isBlocking = callee == "runBlocking" ||
                    qualifiedText.contains("Thread.sleep") ||
                    qualifiedText.contains(".join(") ||
                    qualifiedText.contains(".get(")

                if (isBlocking) {
                    findings += LifecycleFinding(
                        className = className,
                        phase = LifecyclePhase.RUNNING,
                        severity = LifecycleSeverity.WARNING,
                        message = "Potential event-loop blocking call",
                        detail = "Review `$callee` usage inside the verticle; blocking work should run on a worker context.",
                        element = expression.calleeExpression ?: expression
                    )
                }

                super.visitCallExpression(expression)
            }
        })
    }

    private fun analyzeEventBusRequests(
        className: String,
        klass: KtClass,
        findings: MutableList<LifecycleFinding>
    ) {
        klass.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                val callee = expression.calleeExpression?.text
                val qualifiedText = expression.qualifiedText()

                if (callee == "request" && qualifiedText.contains("eventBus") && !qualifiedText.hasFailureHandling()) {
                    findings += LifecycleFinding(
                        className = className,
                        phase = LifecyclePhase.RUNNING,
                        severity = LifecycleSeverity.WARNING,
                        message = "Event bus request failure path is not visible",
                        detail = "Add `onFailure`, `onComplete`, or coroutine exception handling around this request.",
                        element = expression.calleeExpression ?: expression
                    )
                }

                super.visitCallExpression(expression)
            }
        })
    }

    private fun managedFinding(
        className: String,
        phase: LifecyclePhase,
        expression: KtCallExpression,
        message: String,
        detail: String
    ): LifecycleFinding {
        val storage = if (expression.isStored()) "Handle stored." else "Handle not stored."
        return LifecycleFinding(
            className = className,
            phase = phase,
            severity = LifecycleSeverity.INFO,
            message = message,
            detail = "$detail $storage",
            element = expression.calleeExpression ?: expression
        )
    }

    fun superTypeNames(klass: KtClass): Set<String> {
        return klass.superTypeListEntries
            .map { entry -> entry.text.substringBefore("(").substringBefore("<").trim() }
            .filter { it.isNotEmpty() }
            .flatMap { name -> listOf(name, name.substringAfterLast(".")) }
            .toSet()
    }

    fun builtInVerticleTypes(): Set<String> = builtInVerticleTypes

    private fun KtClass.isVertxVerticle(knownVerticleTypes: Set<String>): Boolean {
        return superTypeNames(this).any { it in knownVerticleTypes }
    }

    private fun KtCallExpression.qualifiedText(): String {
        return (parent as? KtDotQualifiedExpression)?.text ?: text
    }

    private fun String.hasFailureHandling(): Boolean {
        return contains("onFailure") || contains("onComplete") || contains("recover") || contains("try")
    }

    private fun KtCallExpression.firstArgumentLabel(): String {
        return valueArguments.firstOrNull()
            ?.getArgumentExpression()
            ?.text
            ?.trim('"')
            ?: "dynamic address"
    }

    private fun KtCallExpression.isStored(): Boolean {
        var current: PsiElement = this
        if (current.parent is KtDotQualifiedExpression) {
            current = current.parent
        }

        val parent = current.parent
        return (parent is KtProperty && parent.initializer == current) ||
            (parent is KtBinaryExpression && parent.operationToken == KtTokens.EQ && parent.right == current)
    }
}
