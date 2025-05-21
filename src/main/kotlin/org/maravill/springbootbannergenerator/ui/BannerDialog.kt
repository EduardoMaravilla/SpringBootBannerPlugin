package org.maravill.springbootbannergenerator.ui

import com.github.lalyos.jfiglet.FigletFont
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import org.maravill.springbootbannergenerator.fonts.FontConstant
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class BannerDialog(project: Project?) : DialogWrapper(project) {

    private val inputTextArea = JTextArea(5,80);
    private val previewArea = JTextArea(20,80);
    private val fontSelector = ComboBox<String>();

    init{
        title = "Spring Boot Banner Generator"
        init()
        isResizable = true
        previewArea.isEditable = false
        previewArea.font = inputTextArea.font
        previewArea.lineWrap = false
        previewArea.wrapStyleWord = false
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout(5,5))

        val inputPanel = JPanel()
        inputPanel.layout = BoxLayout(inputPanel, BoxLayout.Y_AXIS)
        inputPanel.add(JLabel("Enter banner text:"))
        inputPanel.add(JScrollPane(inputTextArea))

        inputPanel.add(Box.createVerticalStrut(10))
        inputPanel.add(JLabel("Select font:"))

        fontSelector.model = DefaultComboBoxModel(FontConstant.fonts.toTypedArray())
        inputPanel.add(fontSelector)

        panel.add(inputPanel, BorderLayout.NORTH)

        val previewPanel = JPanel(BorderLayout())
        previewPanel.border = BorderFactory.createTitledBorder("Preview")
        previewPanel.add(JScrollPane(previewArea), BorderLayout.CENTER)

        panel.add(previewPanel, BorderLayout.CENTER)

        setupListeners()

        return panel
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
                        if (inputStream != null ) {
                            if (line.isNotEmpty() && line.isNotBlank()){
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
                previewArea.text = "Error generating banner" + ex.localizedMessage
            }
        }

        inputTextArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updatePreview()
            override fun removeUpdate(e: DocumentEvent?) = updatePreview()
            override fun changedUpdate(e: DocumentEvent?) = updatePreview()
        })

        fontSelector.addActionListener { updatePreview() }
    }

    fun getBannerText(): String = previewArea.text
    fun getInputLines(): String = inputTextArea.text
    fun getSelectedFont(): String = fontSelector.selectedItem as? String ?: "standard"
}