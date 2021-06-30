package com.intelygenz.android.acceptanceplugin

import com.intelygenz.android.gherkinparser.Feature
import java.awt.*
import java.util.regex.Pattern
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener


fun JComponent.row(vararg components: JComponent, gravity: Int = FlowLayout.LEFT) = horizontalStack(gravity) { components.forEach { add(it) }}
fun JComponent.horizontalStack(gravity: Int = FlowLayout.LEFT, build: JComponent.() -> Unit): JComponent = apply { add(hStack(gravity, build)) }
fun hStack(gravity: Int = FlowLayout.LEFT, build: JComponent.() -> Unit): JComponent = JPanel(FlowLayout(gravity)).apply(build)
fun vStack(axis: Int = BoxLayout.PAGE_AXIS, build: JComponent.() -> Unit): JComponent = JPanel().apply { layout = BoxLayout(this, axis) }.apply(build)

class GenerateCodeDialog(title: String, packageName: String, val onAccept: (String) -> Unit): JDialog() {
    private val packagePattern = Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)*[\\p{L}_$][\\p{L}\\p{N}_$]*")
    private val acceptButton = JButton("Accept")
    private val cancelButton = JButton("Cancel")
    private val packageTitle = JLabel("Package to build:")
    private val packageField = JTextField().apply { preferredSize = Dimension(250, 30) }
    private val errorLabel = JLabel(" ").apply { foreground = Color.RED }

    init {
        preferredSize = Dimension(400, 200)
        this.title = title
        isModal = true
        contentPane = vStack {
            row(packageTitle)
            row(packageField.apply { preferredSize.width = 200 })
            row(packageField.apply { preferredSize.width = 200 })
            row(errorLabel.apply { preferredSize = Dimension(200, 30) })
            row(cancelButton, acceptButton, gravity = FlowLayout.RIGHT)
        }

        packageField.text = packageName

        cancelButton.addActionListener { dispose() }
        packageField.document.addDocumentListener(object: DocumentListener {
            override fun insertUpdate(p0: DocumentEvent?) = validatePackage()
            override fun removeUpdate(p0: DocumentEvent?) = validatePackage()
            override fun changedUpdate(p0: DocumentEvent?) = validatePackage()
        })
        acceptButton.addActionListener { dispose(); onAccept(packageField.text!!) }
        validatePackage()
    }

    private fun validatePackage() {
        val isValid = packagePattern.matcher(packageField.text ?: "").matches()
        errorLabel.text = if(isValid) "" else "Package is not valid"
        acceptButton.isEnabled = isValid
    }
}

