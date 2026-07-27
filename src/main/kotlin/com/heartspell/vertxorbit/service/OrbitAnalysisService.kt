package com.heartspell.vertxorbit.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.heartspell.vertxorbit.lifecycle.VertxLifecycleAnalyzer
import com.heartspell.vertxorbit.navigation.SourceTarget
import com.heartspell.vertxorbit.settings.OrbitMessages
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

class OrbitAnalysisService(
    private val project: Project,
    private val analyzer: VertxLifecycleAnalyzer = VertxLifecycleAnalyzer()
) {
    fun analyzeActiveEditor(): OrbitAnalysisResult {
        return ApplicationManager.getApplication().runReadAction<OrbitAnalysisResult> {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
                ?: return@runReadAction OrbitAnalysisResult(OrbitMessages.noActiveEditor(), emptyList(), emptyList(), emptyList())

            val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            if (file !is KtFile) {
                return@runReadAction OrbitAnalysisResult(file?.name ?: OrbitMessages.currentFile(), emptyList(), emptyList(), emptyList())
            }

            val knownVerticleTypes = resolveKnownVerticleTypes(file)
            val verticleAnalyses = analyzer.analyzeVerticles(file, knownVerticleTypes)
            val deployments = collectDeployments(file)
            val verticles = verticleAnalyses.mapIndexed { verticleIndex, analysis ->
                val findings = analysis.findings.mapIndexed { findingIndex, finding ->
                    OrbitFindingEntry(
                        id = "$verticleIndex:$findingIndex",
                        line = finding.lineLabel(),
                        finding = finding,
                        target = finding.element.sourceTarget()
                    )
                }
                ActiveVerticleEntry(
                    id = verticleIndex.toString(),
                    className = analysis.className,
                    line = analysis.element.lineLabel(),
                    target = analysis.element.sourceTarget(),
                    findings = findings
                )
            }

            OrbitAnalysisResult(
                fileName = file.name,
                verticles = verticles,
                findings = verticles.flatMap { it.findings },
                deployments = deployments
            )
        }
    }

    private fun resolveKnownVerticleTypes(file: KtFile): Set<String> {
        val declarations = mutableMapOf<String, Set<String>>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                val name = klass.name
                if (name != null) {
                    declarations[name] = analyzer.superTypeNames(klass)
                }
                super.visitClass(klass)
            }
        })

        val known = analyzer.builtInVerticleTypes().toMutableSet()
        var changed: Boolean
        do {
            changed = false
            declarations.forEach { (className, supers) ->
                if (className !in known && supers.any { it in known }) {
                    known += className
                    changed = true
                }
            }
        } while (changed)
        return known
    }

    private fun collectDeployments(file: KtFile): List<ActiveDeploymentEntry> {
        val deployments = mutableListOf<ActiveDeploymentEntry>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitCallExpression(expression: KtCallExpression) {
                if (expression.calleeExpression?.text == "deployVerticle") {
                    val target = expression.valueArguments.firstOrNull()?.getArgumentExpression()?.text.orEmpty()
                    val simpleName = target.toDeployTargetSimpleName()
                    deployments += ActiveDeploymentEntry(
                        id = deployments.size.toString(),
                        target = target.ifEmpty { OrbitMessages.dynamicDeployTarget() },
                        targetSimpleName = simpleName,
                        line = expression.lineLabel(),
                        source = (expression.calleeExpression ?: expression).sourceTarget(),
                        isDynamic = simpleName == null
                    )
                }
                super.visitCallExpression(expression)
            }
        })
        return deployments
    }

    private fun String.toDeployTargetSimpleName(): String? {
        val trimmed = trim()
        if (trimmed.isEmpty()) return null
        val constructorName = trimmed.substringBefore("(")
        if (constructorName != trimmed && constructorName.isNotBlank()) {
            return constructorName.substringAfterLast(".")
        }
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.trim('"').substringAfterLast(".")
        }
        return null
    }

    private fun com.heartspell.vertxorbit.lifecycle.LifecycleFinding.lineLabel(): String {
        val document = element.containingFile?.viewProvider?.document ?: return ""
        return OrbitMessages.line(document.getLineNumber(element.textOffset) + 1)
    }

    private fun com.intellij.psi.PsiElement.lineLabel(): String {
        val document = containingFile?.viewProvider?.document ?: return ""
        return OrbitMessages.line(document.getLineNumber(textOffset) + 1)
    }

    private fun PsiElement.sourceTarget(): SourceTarget {
        val pointer = SmartPointerManager.getInstance(project).createSmartPsiElementPointer(this)
        return SourceTarget(pointer)
    }
}
