package com.heartspell.vertxorbit.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.heartspell.vertxorbit.lifecycle.LifecyclePhase
import com.heartspell.vertxorbit.lifecycle.LifecycleSeverity
import com.heartspell.vertxorbit.navigation.SourceNavigator
import com.heartspell.vertxorbit.settings.OrbitMessages
import com.heartspell.vertxorbit.settings.OrbitSettingsListener
import com.heartspell.vertxorbit.settings.VertxOrbitSettings
import com.heartspell.vertxorbit.service.ActiveDeploymentEntry
import com.heartspell.vertxorbit.service.ActiveVerticleEntry
import com.heartspell.vertxorbit.service.OrbitAnalysisResult
import com.heartspell.vertxorbit.service.OrbitAnalysisService
import com.heartspell.vertxorbit.service.OrbitFindingEntry
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.ToolTipManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class VertxOrbitPanel(
    project: Project,
    private val analysisService: OrbitAnalysisService = OrbitAnalysisService(project),
    private val navigator: SourceNavigator = SourceNavigator(project)
) {
    val component: JPanel = JPanel(BorderLayout())

    private val subtitle = JBLabel(OrbitMessages.activeEditor())
    private val tree = JTree(DefaultMutableTreeNode(Node.Root))
    private val treePane = JBScrollPane(tree)
    private val content = JPanel(BorderLayout())
    private val detailsPanel = SelectionDetailsPanel()
    private var currentResult = OrbitAnalysisResult(OrbitMessages.activeEditor(), emptyList(), emptyList(), emptyList())

    init {
        component.background = UIUtil.getPanelBackground()
        component.border = JBUI.Borders.empty(6)
        component.minimumSize = Dimension(0, 0)

        subtitle.font = subtitle.font.deriveFont(Font.BOLD, 12f)
        subtitle.foreground = JBColor.GRAY

        val refresh = JButton(AllIcons.Actions.Refresh)
        refresh.toolTipText = OrbitMessages.refreshActiveFile()
        refresh.isFocusable = false
        refresh.addActionListener { refresh() }

        val labels = JPanel(BorderLayout())
        labels.isOpaque = false
        labels.add(subtitle, BorderLayout.CENTER)

        val header = JPanel(BorderLayout(6, 0))
        header.isOpaque = false
        header.border = JBUI.Borders.empty(0, 0, 6, 0)
        header.add(labels, BorderLayout.CENTER)
        header.add(refresh, BorderLayout.EAST)

        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.rowHeight = JBUI.scale(22)
        tree.border = JBUI.Borders.empty(2)
        tree.background = UIUtil.getTreeBackground()
        tree.minimumSize = Dimension(0, 0)
        tree.cellRenderer = OrbitTreeRenderer()
        ToolTipManager.sharedInstance().registerComponent(tree)
        tree.addTreeSelectionListener {
            updateDetails()
        }
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount >= 2) {
                    selectedNode()?.navigate()
                }
            }
        })

        treePane.border = BorderFactory.createLineBorder(JBColor.border())
        treePane.minimumSize = Dimension(0, 0)
        content.background = UIUtil.getPanelBackground()
        content.minimumSize = Dimension(0, 0)
        content.add(treePane, BorderLayout.CENTER)

        val north = JPanel(BorderLayout())
        north.isOpaque = false
        north.minimumSize = Dimension(0, 0)
        north.add(header, BorderLayout.NORTH)

        component.add(north, BorderLayout.NORTH)
        component.add(content, BorderLayout.CENTER)

        project.messageBus.connect(project).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    refresh()
                }
            }
        )
        ApplicationManager.getApplication().messageBus.connect(project).subscribe(
            OrbitSettingsListener.TOPIC,
            OrbitSettingsListener { refresh() }
        )

        ApplicationManager.getApplication().invokeLater { refresh() }
    }

    private fun refresh() {
        currentResult = analysisService.analyzeActiveEditor()
        subtitle.text = currentResult.fileName
        renderTree(currentResult)
    }

    private fun renderTree(result: OrbitAnalysisResult) {
        val root = DefaultMutableTreeNode(Node.Root)

        if (result.verticles.isEmpty()) {
            root.add(DefaultMutableTreeNode(Node.Empty(OrbitMessages.noVerticle())))
        } else {
            result.verticles.forEach { verticle ->
                root.add(verticleNode(verticle, result.deployments))
            }
        }

        tree.model = DefaultTreeModel(root)
        expandVerticles()
        selectFirstUsefulNode(root)
    }

    private fun verticleNode(
        verticle: ActiveVerticleEntry,
        deployments: List<ActiveDeploymentEntry>
    ): DefaultMutableTreeNode {
        val root = DefaultMutableTreeNode(Node.Verticle(verticle))
        val relatedDeployments = deployments.filter { deployment ->
            deployment.targetSimpleName == verticle.className || deployment.target == verticle.className
        }

        if (relatedDeployments.isNotEmpty()) {
            val deployGroup = DefaultMutableTreeNode(Node.Group(OrbitMessages.deployments(), AllIcons.Nodes.RunnableMark))
            relatedDeployments.forEach { deployGroup.add(DefaultMutableTreeNode(Node.Deployment(it, verticle))) }
            root.add(deployGroup)
        }

        if (verticle.findings.isEmpty()) {
            root.add(DefaultMutableTreeNode(Node.Empty(OrbitMessages.noSignals())))
        } else {
            LifecyclePhase.entries.forEach { phase ->
                val phaseFindings = verticle.findings.filter { it.finding.phase == phase }
                if (phaseFindings.isNotEmpty()) {
                    val phaseGroup = DefaultMutableTreeNode(Node.Phase(phase, phaseFindings, verticle))
                    phaseFindings.forEach { phaseGroup.add(DefaultMutableTreeNode(Node.Finding(it, verticle))) }
                    root.add(phaseGroup)
                }
            }
        }

        return root
    }

    private fun expandVerticles() {
        for (row in 0 until tree.rowCount) {
            tree.expandRow(row)
        }
    }

    private fun selectFirstUsefulNode(root: DefaultMutableTreeNode) {
        val first = root.firstChild as? DefaultMutableTreeNode
        val path = first?.let { TreePath(it.path) } ?: TreePath(root.path)
        tree.selectionPath = path
        updateDetails()
    }

    private fun updateDetails() {
        content.remove(detailsPanel.component)
        val node = selectedNode()
        if (VertxOrbitSettings.getInstance().displayMode != VertxOrbitSettings.DisplayMode.TRACE) {
            detailsPanel.showNode(node, currentResult, VertxOrbitSettings.getInstance().displayMode)
            content.add(detailsPanel.component, BorderLayout.SOUTH)
        }
        content.revalidate()
        content.repaint()
    }

    private fun selectedNode(): Node? {
        val node = tree.lastSelectedPathComponent as? DefaultMutableTreeNode ?: return null
        return node.userObject as? Node
    }

    private fun Node.navigate() {
        when (this) {
            is Node.Verticle -> navigator.navigateTo(verticle.target)
            is Node.Deployment -> navigator.navigateTo(deployment.source)
            is Node.Finding -> navigator.navigateTo(entry.target)
            else -> Unit
        }
    }

    private fun iconFor(node: Node): Icon {
        return when (node) {
            Node.Root -> AllIcons.Nodes.Module
            is Node.Verticle -> if (node.verticle.issues == 0) AllIcons.Nodes.Class else AllIcons.Nodes.ExceptionClass
            is Node.Group -> node.icon
            is Node.Phase -> phaseIcon(node)
            is Node.Deployment -> AllIcons.Nodes.RunnableMark
            is Node.Finding -> if (node.entry.finding.severity == LifecycleSeverity.WARNING) {
                AllIcons.General.Warning
            } else {
                AllIcons.General.InspectionsOK
            }
            is Node.Empty -> AllIcons.General.Information
        }
    }

    private fun phaseIcon(node: Node.Phase): Icon {
        val hasIssue = node.findings.any { it.finding.severity == LifecycleSeverity.WARNING }
        return if (hasIssue) AllIcons.General.Warning else AllIcons.Actions.Checked
    }

    private fun colorFor(node: Node): Color {
        return when {
            node is Node.Finding && node.entry.finding.severity == LifecycleSeverity.WARNING -> warningColor()
            node is Node.Verticle && node.verticle.issues > 0 -> warningColor()
            node is Node.Phase && node.findings.any { it.finding.severity == LifecycleSeverity.WARNING } -> warningColor()
            node is Node.Empty -> JBColor.GRAY
            else -> UIUtil.getTreeForeground()
        }
    }

    private fun labelFor(node: Node): String {
        return when (node) {
            Node.Root -> "Vert.x Orbit"
            is Node.Verticle -> {
                val state = if (node.verticle.issues == 0) OrbitMessages.ok() else OrbitMessages.review()
                "${node.verticle.className}  ${node.verticle.line}  $state"
            }
            is Node.Group -> node.title
            is Node.Phase -> "${OrbitMessages.phaseTitle(node.phase)}  ${node.findings.size}"
            is Node.Deployment -> "deployVerticle  ${node.deployment.target}  ${node.deployment.line}"
            is Node.Finding -> "${OrbitMessages.findingMessage(node.entry.finding)}  ${node.entry.line}"
            is Node.Empty -> node.text
        }
    }

    private fun tooltipFor(node: Node): String {
        return when (node) {
            Node.Root -> currentResult.fileName
            is Node.Verticle -> {
                val state = if (node.verticle.issues == 0) OrbitMessages.ok() else OrbitMessages.needsReview()
                "${node.verticle.line}. $state. ${OrbitMessages.openClassHint()}"
            }
            is Node.Group -> OrbitMessages.openChildrenHint()
            is Node.Phase -> "${OrbitMessages.phaseTitle(node.phase)}: ${node.findings.size} ${OrbitMessages.lifecycleSignals(node.findings.size)}."
            is Node.Deployment -> "${node.deployment.target} at ${node.deployment.line}. ${OrbitMessages.openDeployHint()}"
            is Node.Finding -> "${OrbitMessages.findingDetail(node.entry.finding)} ${node.entry.line}. ${OrbitMessages.openSourceHint()}"
            is Node.Empty -> node.text
        }
    }

    private fun descriptionFor(node: Node?): String {
        return when (node) {
            is Node.Verticle -> OrbitMessages.selectedVerticleDescription(node.verticle.className)
            is Node.Phase -> "${OrbitMessages.phaseTitle(node.phase)}: ${node.findings.size} ${OrbitMessages.lifecycleSignals(node.findings.size)}."
            is Node.Deployment -> OrbitMessages.selectedDeploymentDescription(node.deployment.target)
            is Node.Finding -> OrbitMessages.selectedFindingDescription(OrbitMessages.findingMessage(node.entry.finding))
            is Node.Empty -> node.text
            else -> currentResult.fileName
        }
    }

    private fun recommendationFor(node: Node?): String {
        return when {
            node is Node.Finding && node.entry.finding.severity == LifecycleSeverity.WARNING -> OrbitMessages.warningRecommendation()
            node is Node.Deployment && node.deployment.isDynamic -> OrbitMessages.dynamicDeployRecommendation()
            node is Node.Verticle && node.verticle.issues > 0 -> OrbitMessages.warningRecommendation()
            node is Node.Phase && node.findings.any { it.finding.severity == LifecycleSeverity.WARNING } -> OrbitMessages.warningRecommendation()
            else -> OrbitMessages.okRecommendation()
        }
    }

    private fun verticleFor(node: Node, result: OrbitAnalysisResult): ActiveVerticleEntry? {
        return when (node) {
            is Node.Verticle -> node.verticle
            is Node.Phase -> node.verticle
            is Node.Deployment -> node.verticle
            is Node.Finding -> node.verticle
            else -> result.verticles.firstOrNull()
        }
    }

    private fun phaseColor(phase: LifecyclePhase): Color {
        return when (phase) {
            LifecyclePhase.START -> JBColor(Color(0x315FAD), Color(0x7AA2F7))
            LifecyclePhase.RUNNING -> JBColor(Color(0x0F766E), Color(0x5EEAD4))
            LifecyclePhase.STOP -> JBColor(Color(0xB91C1C), Color(0xFCA5A5))
        }
    }

    private fun warningColor(): Color = JBColor(Color(0xB45309), Color(0xF59E0B))

    private sealed interface Node {
        data object Root : Node
        data class Verticle(val verticle: ActiveVerticleEntry) : Node
        data class Group(val title: String, val icon: Icon) : Node
        data class Phase(
            val phase: LifecyclePhase,
            val findings: List<OrbitFindingEntry>,
            val verticle: ActiveVerticleEntry
        ) : Node
        data class Deployment(val deployment: ActiveDeploymentEntry, val verticle: ActiveVerticleEntry) : Node
        data class Finding(val entry: OrbitFindingEntry, val verticle: ActiveVerticleEntry) : Node
        data class Empty(val text: String) : Node
    }

    private inner class OrbitTreeRenderer : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
            val node = (value as? DefaultMutableTreeNode)?.userObject as? Node ?: return component
            text = labelFor(node)
            icon = iconFor(node)
            if (!selected) {
                foreground = colorFor(node)
            }
            toolTipText = tooltipFor(node)
            border = JBUI.Borders.empty(1, 2)
            return component
        }
    }

    private inner class SelectionDetailsPanel {
        val component = JPanel(BorderLayout(JBUI.scale(8), 0))
        private val graph = LifecycleStatusGraph()

        init {
            component.background = UIUtil.getPanelBackground()
            component.minimumSize = Dimension(0, 0)
        }

        fun showNode(node: Node?, result: OrbitAnalysisResult, mode: VertxOrbitSettings.DisplayMode) {
            component.removeAll()
            component.border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtil.getBoundsColor()),
                JBUI.Borders.empty(6, 6)
            )
            graph.setVerticle(node?.let { verticleFor(it, result) })
            component.add(graph, BorderLayout.WEST)

            val textPanel = JPanel(BorderLayout())
            textPanel.isOpaque = false
            textPanel.minimumSize = Dimension(0, 0)

            when (mode) {
                VertxOrbitSettings.DisplayMode.FULL -> {
                    component.preferredSize = Dimension(JBUI.scale(220), JBUI.scale(132))
                    val text = JPanel(BorderLayout(0, JBUI.scale(4)))
                    text.isOpaque = false
                    text.add(textBlock(OrbitMessages.description(), descriptionFor(node)), BorderLayout.NORTH)
                    text.add(textBlock(OrbitMessages.recommendation(), recommendationFor(node)), BorderLayout.CENTER)
                    textPanel.add(text, BorderLayout.CENTER)
                }

                VertxOrbitSettings.DisplayMode.TINY -> {
                    component.preferredSize = Dimension(JBUI.scale(180), JBUI.scale(72))
                    textPanel.add(tinyText(descriptionFor(node), recommendationFor(node)), BorderLayout.CENTER)
                }

                VertxOrbitSettings.DisplayMode.TRACE -> Unit
            }

            component.add(textPanel, BorderLayout.CENTER)
            component.revalidate()
            component.repaint()
        }

        private fun textBlock(title: String, text: String): JComponent {
            val panel = JPanel(BorderLayout())
            panel.isOpaque = false
            val label = JBLabel(title)
            label.font = label.font.deriveFont(Font.BOLD, 11f)
            val area = textArea(text, 2)
            panel.add(label, BorderLayout.NORTH)
            panel.add(area, BorderLayout.CENTER)
            return panel
        }

        private fun tinyText(description: String, recommendation: String): JComponent {
            return textArea("$description  $recommendation", 2)
        }

        private fun textArea(text: String, rows: Int): JBTextArea {
            val area = JBTextArea(text)
            area.rows = rows
            area.isEditable = false
            area.isOpaque = false
            area.lineWrap = true
            area.wrapStyleWord = true
            area.font = UIUtil.getLabelFont()
            area.foreground = UIUtil.getLabelForeground()
            area.border = JBUI.Borders.emptyTop(2)
            area.minimumSize = Dimension(0, 0)
            return area
        }
    }

    private inner class LifecycleStatusGraph : JComponent() {
        private var verticle: ActiveVerticleEntry? = null

        init {
            preferredSize = Dimension(JBUI.scale(180), JBUI.scale(58))
            minimumSize = Dimension(0, 0)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, UIUtil.getBoundsColor()),
                JBUI.Borders.empty(5, 6)
            )
        }

        fun setVerticle(value: ActiveVerticleEntry?) {
            verticle = value
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            val phases = LifecyclePhase.entries
            val contentWidth = (width - JBUI.scale(24)).coerceAtLeast(JBUI.scale(132))
            val y = JBUI.scale(22)
            val startX = JBUI.scale(12)
            val gap = contentWidth / (phases.size - 1)
            val radius = JBUI.scale(7)
            val findings = verticle?.findings.orEmpty()

            g2.stroke = BasicStroke(JBUIScale.scale(1.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
            g2.color = UIUtil.getBoundsColor()
            g2.drawLine(startX + radius, y, startX + gap * (phases.size - 1) - radius, y)

            phases.forEachIndexed { index, phase ->
                val x = startX + gap * index
                val phaseFindings = findings.filter { it.finding.phase == phase }
                val hasIssue = phaseFindings.any { it.finding.severity == LifecycleSeverity.WARNING }
                val active = phaseFindings.isNotEmpty()
                val status = phaseState(phase, active, hasIssue)

                g2.color = when {
                    hasIssue -> warningColor()
                    active -> phaseColor(phase)
                    else -> UIUtil.getBoundsColor()
                }
                g2.fillOval(x - radius, y - radius, radius * 2, radius * 2)

                g2.color = UIUtil.getPanelBackground()
                g2.fillOval(x - JBUI.scale(3), y - JBUI.scale(3), JBUI.scale(6), JBUI.scale(6))

                g2.color = UIUtil.getLabelForeground()
                g2.font = UIUtil.getLabelFont().deriveFont(9f)
                g2.drawString(OrbitMessages.phaseShort(phase), x - JBUI.scale(13), y + JBUI.scale(20))

                g2.color = if (hasIssue) warningColor() else UIUtil.getLabelDisabledForeground()
                g2.font = UIUtil.getLabelFont().deriveFont(Font.BOLD, 9f)
                g2.drawString(status, x - JBUI.scale(13), y + JBUI.scale(34))
            }
        }

        private fun phaseState(phase: LifecyclePhase, active: Boolean, hasIssue: Boolean): String {
            return when {
                hasIssue -> OrbitMessages.review()
                phase == LifecyclePhase.RUNNING && active -> OrbitMessages.runningStatus()
                active -> "ok"
                else -> "-"
            }
        }
    }
}
