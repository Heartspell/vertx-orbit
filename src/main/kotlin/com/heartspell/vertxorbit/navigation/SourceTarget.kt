package com.heartspell.vertxorbit.navigation

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer

data class SourceTarget(
    val pointer: SmartPsiElementPointer<PsiElement>
)
