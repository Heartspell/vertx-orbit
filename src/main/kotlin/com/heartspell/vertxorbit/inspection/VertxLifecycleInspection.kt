package com.heartspell.vertxorbit.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.heartspell.vertxorbit.lifecycle.LifecycleSeverity
import com.heartspell.vertxorbit.lifecycle.VertxLifecycleAnalyzer
import com.heartspell.vertxorbit.settings.OrbitMessages
import org.jetbrains.kotlin.psi.KtFile

class VertxLifecycleInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file !is KtFile) {
                    return
                }

                VertxLifecycleAnalyzer()
                    .analyze(file)
                    .filter { it.severity == LifecycleSeverity.WARNING }
                    .forEach { finding ->
                        holder.registerProblem(
                            finding.element,
                            "${OrbitMessages.findingMessage(finding)}: ${OrbitMessages.findingDetail(finding)}"
                        )
                    }
            }
        }
    }
}
