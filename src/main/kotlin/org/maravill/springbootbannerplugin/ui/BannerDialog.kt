package org.maravill.springbootbannerplugin.ui

import com.github.lalyos.jfiglet.FigletFont
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import org.maravill.springbootbannerplugin.fonts.FontConstant
import java.awt.*
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


class BannerDialog(project: Project?) : DialogWrapper(project) {

    private val inputTextArea = JTextArea(5, 80)
    private val previewArea = JTextArea(20, 80)
    private val fontSelector = ComboBox<String>()
    private val fontFilterField = JTextArea(1, 80)

    init {
        title = "Spring Boot Banner Plugin"
        init()
        isResizable = true
        setupStyle()
    }

    private fun setupStyle() {
        val font = Font("Monospaced", Font.PLAIN, 16)

        inputTextArea.font = font
        inputTextArea.border = CompoundBorder(LineBorder(JBColor.GRAY, 1), JBUI.Borders.empty(5))
        inputTextArea.margin = JBUI.insets(10)

        fontFilterField.font = font
        fontFilterField.border = CompoundBorder(LineBorder(JBColor.GRAY, 1), JBUI.Borders.empty(5))
        fontFilterField.margin = JBUI.insets(5)

        previewArea.isEditable = false
        previewArea.font = font.deriveFont(Font.BOLD, 14f)
        previewArea.lineWrap = false
        previewArea.wrapStyleWord = false
        previewArea.border = CompoundBorder(LineBorder(JBColor.DARK_GRAY, 1), JBUI.Borders.empty(10))

        fontSelector.font = font
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout(10, 10))
        panel.border = JBUI.Borders.empty(10)

        val inputPanel = JPanel()
        inputPanel.layout = BoxLayout(inputPanel, BoxLayout.Y_AXIS)

        inputPanel.add(label("Enter banner text:"))
        inputPanel.add(JScrollPane(inputTextArea))

        inputPanel.add(Box.createVerticalStrut(10))
        inputPanel.add(label("Filter fonts:"))
        inputPanel.add(fontFilterField)

        inputPanel.add(Box.createVerticalStrut(10))
        inputPanel.add(label("Select font:"))
        updateFontList()
        inputPanel.add(fontSelector)

        panel.add(inputPanel, BorderLayout.NORTH)

        val previewPanel = JPanel(BorderLayout())
        previewPanel.border = BorderFactory.createTitledBorder("Preview")
        previewPanel.add(JScrollPane(previewArea), BorderLayout.CENTER)

        panel.add(previewPanel, BorderLayout.CENTER)

        setupListeners()

        return panel
    }

    private fun label(text: String): JLabel {
        val label = JLabel(text)
        label.font = Font("SansSerif", Font.BOLD, 15)
        label.alignmentX = Component.CENTER_ALIGNMENT
        label.horizontalAlignment = SwingConstants.CENTER
        return label
    }


    private fun updateFontList(filter: String = "") {
        val filteredFonts = FontConstant.fonts.filter {
            it.contains(filter, ignoreCase = true)
        }.sorted()
        fontSelector.model = DefaultComboBoxModel(filteredFonts.toTypedArray())
    }

    private fun setupListeners() {
        val updatePreview = let@{
            try {
                val font = fontSelector.selectedItem as? String ?: "standard"
                val text = inputTextArea.text

                if (text.isBlank()) {
                    previewArea.text = ""
                    return@let
                }

                val banner = buildString {
                    for (line in text.lines()) {
                        val inputStream = BannerDialog::class.java.getResourceAsStream(
                            "/" + FontConstant.FONT_FOLDER_LOCATION + "/" + font + FontConstant.FONT_EXTENSION
                        )
                        if (inputStream != null) {
                            if (line.isNotEmpty()) {
                                append(FigletFont.convertOneLine(inputStream, line))
                            }
                            inputStream.close()
                        } else {
                            append("Error: font not found\n")
                            break
                        }
                    }
                }
                previewArea.text = banner
            } catch (ex: Exception) {
                previewArea.text = "Error generating banner: ${ex.localizedMessage}"
            }
        }

        inputTextArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updatePreview()
            override fun removeUpdate(e: DocumentEvent?) = updatePreview()
            override fun changedUpdate(e: DocumentEvent?) = updatePreview()
        })

        fontSelector.addActionListener { updatePreview() }

        fontFilterField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = applyFilter()
            override fun removeUpdate(e: DocumentEvent?) = applyFilter()
            override fun changedUpdate(e: DocumentEvent?) = applyFilter()

            private fun applyFilter() {
                val currentFilter = fontFilterField.text
                val currentSelection = fontSelector.selectedItem as? String
                updateFontList(currentFilter)

                if (currentSelection != null && FontConstant.fonts.contains(currentSelection)) {
                    fontSelector.selectedItem = currentSelection
                }
            }
        })
    }

    fun getBannerText(): String = previewArea.text
}