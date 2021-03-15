package ch.lightspots.it.intellij.plugin.generate.builder.java.options

import ch.lightspots.it.intellij.plugin.generate.builder.ext.getBoolean
import ch.lightspots.it.intellij.plugin.generate.builder.ext.getValue
import ch.lightspots.it.intellij.plugin.generate.builder.ext.setValue
import com.intellij.ide.util.PropertiesComponent
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object Options {
    private val propertiesComponent = PropertiesComponent.getInstance()
    val availableOptions: List<OptionEntry>

    init {
        val staticBuilderNameTextField = JTextField("builder").apply {
            propertiesComponent.getValue(OptionProperty.STATIC_BUILDER_METHOD_NAME)?.let {
                text = it
            }
            val updateProperty = {
                propertiesComponent.setValue(OptionProperty.STATIC_BUILDER_METHOD_NAME, text)
            }
            document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    updateProperty()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    updateProperty()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    updateProperty()
                }
            })
        }
        val staticBuilderMethodCheckBox = JCheckBox("Generate static builder method").apply {
            isSelected = propertiesComponent.getBoolean(OptionProperty.STATIC_BUILDER_METHOD)
            addActionListener {
                propertiesComponent.setValue(OptionProperty.STATIC_BUILDER_METHOD, isSelected)
                staticBuilderNameTextField.isEnabled = isSelected
            }
        }

        availableOptions = listOf(
            OptionEntry(OptionProperty.STATIC_BUILDER_METHOD, OptionType.CHECKBOX, staticBuilderMethodCheckBox),
            OptionEntry(
                OptionProperty.STATIC_BUILDER_METHOD_NAME,
                OptionType.TEXT,
                JPanel().apply {
                    add(staticBuilderNameTextField)
                    add(JLabel("Name of static builder method"))
                }
            )
        )
    }
}

data class OptionEntry(val property: OptionProperty, val type: OptionType, val component: JComponent)
