package com.heartspell.vertxorbit.lifecycle

import com.intellij.psi.PsiElement

enum class LifecyclePhase(val title: String) {
    START("Start"),
    RUNNING("Running"),
    STOP("Stop")
}

enum class LifecycleSeverity(val title: String) {
    INFO("Managed"),
    WARNING("Warning")
}

data class LifecycleFinding(
    val className: String,
    val phase: LifecyclePhase,
    val severity: LifecycleSeverity,
    val message: String,
    val detail: String,
    val element: PsiElement
)

data class VerticleAnalysis(
    val className: String,
    val element: PsiElement,
    val findings: List<LifecycleFinding>
)
