package com.heartspell.vertxorbit.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class VertxOrbitToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val component = VertxOrbitPanel(project).component
        val content = ContentFactory.getInstance().createContent(component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
