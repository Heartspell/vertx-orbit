package com.heartspell.vertxorbit.settings

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class VertxOrbitConfigurable : Configurable {
    private var panel: JPanel? = null
    private var languageBox: JComboBox<VertxOrbitSettings.OrbitLanguage>? = null
    private var displayModeBox: JComboBox<VertxOrbitSettings.DisplayMode>? = null

    private val titleLabel = JBLabel("Vert.x Orbit")
    private val introLabel = JBLabel()
    private val languageLabel = JBLabel()
    private val displayModeLabel = JBLabel()
    private val authorLabel = JBLabel()
    private val repositoryLabel = JBLabel()
    private val aboutLabel = JBLabel()
    private val aboutText = JBLabel()

    override fun getDisplayName(): String = "Vert.x Orbit"

    override fun createComponent(): JComponent {
        val root = JPanel(BorderLayout())
        root.border = JBUI.Borders.empty(12)
        root.background = UIUtil.getPanelBackground()

        val form = JPanel(GridBagLayout())
        form.isOpaque = false

        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 16f)
        addRow(form, 0, null, titleLabel)
        addRow(form, 1, null, introLabel)

        val combo = JComboBox(VertxOrbitSettings.OrbitLanguage.entries.toTypedArray())
        combo.renderer = LanguageRenderer()
        combo.addActionListener { updateTexts(selectedLanguage()) }
        languageBox = combo
        addRow(form, 2, languageLabel, combo)

        val modeCombo = JComboBox(VertxOrbitSettings.DisplayMode.entries.toTypedArray())
        modeCombo.renderer = DisplayModeRenderer { selectedLanguage() }
        displayModeBox = modeCombo
        addRow(form, 3, displayModeLabel, modeCombo)

        val github = JButton("GitHub: heartspell/vertx-orbit")
        github.addActionListener { BrowserUtil.browse(REPOSITORY_URL) }
        addRow(form, 4, authorLabel, JBLabel("amirhanordobaev (heartspell)"))
        addRow(form, 5, repositoryLabel, github)

        addRow(form, 6, aboutLabel, aboutText)

        root.add(form, BorderLayout.NORTH)
        panel = root
        reset()
        return root
    }

    override fun isModified(): Boolean {
        val settings = VertxOrbitSettings.getInstance()
        return languageBox?.selectedItem != settings.language ||
            displayModeBox?.selectedItem != settings.displayMode
    }

    override fun apply() {
        val settings = VertxOrbitSettings.getInstance()
        val selectedLanguage = languageBox?.selectedItem as? VertxOrbitSettings.OrbitLanguage
        val selectedMode = displayModeBox?.selectedItem as? VertxOrbitSettings.DisplayMode
        if (selectedLanguage != null) {
            settings.language = selectedLanguage
        }
        if (selectedMode != null) {
            settings.displayMode = selectedMode
        }
        ApplicationManager.getApplication().messageBus
            .syncPublisher(OrbitSettingsListener.TOPIC)
            .settingsChanged()
    }

    override fun reset() {
        val settings = VertxOrbitSettings.getInstance()
        languageBox?.selectedItem = settings.language
        displayModeBox?.selectedItem = settings.displayMode
        updateTexts(settings.language)
    }

    override fun disposeUIResources() {
        panel = null
        languageBox = null
        displayModeBox = null
    }

    private fun selectedLanguage(): VertxOrbitSettings.OrbitLanguage {
        return languageBox?.selectedItem as? VertxOrbitSettings.OrbitLanguage
            ?: VertxOrbitSettings.getInstance().language
    }

    private fun updateTexts(language: VertxOrbitSettings.OrbitLanguage) {
        introLabel.text = OrbitMessages.settingsIntro(language)
        languageLabel.text = OrbitMessages.languageLabel(language)
        displayModeLabel.text = OrbitMessages.designModeLabel(language)
        authorLabel.text = OrbitMessages.authorLabel(language)
        repositoryLabel.text = OrbitMessages.repositoryLabel(language)
        aboutLabel.text = OrbitMessages.aboutLabel(language)
        aboutText.text = "<html><body style='width:420px'>${OrbitMessages.aboutText(language)}</body></html>"
        displayModeBox?.repaint()
        panel?.revalidate()
        panel?.repaint()
    }

    private fun addRow(form: JPanel, row: Int, label: JBLabel?, field: JComponent) {
        val labelConstraints = GridBagConstraints()
        labelConstraints.gridx = 0
        labelConstraints.gridy = row
        labelConstraints.anchor = GridBagConstraints.WEST
        labelConstraints.insets = Insets(0, 0, JBUI.scale(8), JBUI.scale(8))

        val fieldConstraints = GridBagConstraints()
        fieldConstraints.gridx = 1
        fieldConstraints.gridy = row
        fieldConstraints.weightx = 1.0
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL
        fieldConstraints.anchor = GridBagConstraints.WEST
        fieldConstraints.insets = Insets(0, 0, JBUI.scale(8), 0)

        if (label != null) {
            label.foreground = UIUtil.getLabelForeground()
            form.add(label, labelConstraints)
        }
        form.add(field, fieldConstraints)
    }

    private class LanguageRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            if (value is VertxOrbitSettings.OrbitLanguage) {
                text = OrbitMessages.languageName(value)
            }
            return component
        }
    }

    private class DisplayModeRenderer(
        private val languageProvider: () -> VertxOrbitSettings.OrbitLanguage
    ) : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            if (value is VertxOrbitSettings.DisplayMode) {
                text = OrbitMessages.displayModeName(value, languageProvider())
            }
            return component
        }
    }

    private companion object {
        const val REPOSITORY_URL = "https://github.com/heartspell/vertx-orbit"
    }
}
