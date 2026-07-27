package com.heartspell.vertxorbit.navigation

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.heartspell.vertxorbit.lifecycle.LifecycleFinding
import com.intellij.psi.PsiInvalidElementAccessException
import com.intellij.psi.PsiElement

class SourceNavigator(private val project: Project) {
    fun navigateTo(finding: LifecycleFinding) {
        navigateTo(finding.element)
    }

    fun navigateTo(target: SourceTarget) {
        val descriptor = try {
            ApplicationManager.getApplication().runReadAction<OpenFileDescriptor?> {
                val element = target.pointer.element ?: return@runReadAction null
                if (!element.isValid) return@runReadAction null
                val virtualFile = element.containingFile?.virtualFile ?: return@runReadAction null
                OpenFileDescriptor(project, virtualFile, element.textOffset)
            }
        } catch (_: PsiInvalidElementAccessException) {
            null
        }
        descriptor?.navigate(true)
    }

    fun navigateTo(element: PsiElement) {
        val descriptor = try {
            ApplicationManager.getApplication().runReadAction<OpenFileDescriptor?> {
                if (!element.isValid) return@runReadAction null
                val virtualFile = element.containingFile?.virtualFile ?: return@runReadAction null
                OpenFileDescriptor(project, virtualFile, element.textOffset)
            }
        } catch (_: PsiInvalidElementAccessException) {
            null
        }
        descriptor?.navigate(true)
    }
}
