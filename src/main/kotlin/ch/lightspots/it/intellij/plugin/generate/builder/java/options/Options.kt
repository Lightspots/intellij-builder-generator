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
            isEnabled = propertiesComponent.getBoolean(OptionProperty.STATIC_BUILDER_METHOD)
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

        val builderMethodsPrefix = JTextField("").apply {
            propertiesComponent.getValue(OptionProperty.BUILDER_METHOD_PREFIX)?.let {
                text = it
            }
            val updateProperty = {
                propertiesComponent.setValue(OptionProperty.BUILDER_METHOD_PREFIX, text)
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

        availableOptions = listOf(
            OptionEntry(OptionProperty.STATIC_BUILDER_METHOD, OptionType.CHECKBOX, staticBuilderMethodCheckBox),
            OptionEntry(
                OptionProperty.STATIC_BUILDER_METHOD_NAME,
                OptionType.TEXT,
                JPanel().apply {
                    add(staticBuilderNameTextField)
                    add(JLabel("Name of static builder method"))
                }
            ),
            OptionEntry(
                OptionProperty.BUILDER_METHOD_PREFIX,
                OptionType.TEXT,
                JPanel().apply {
                    add(builderMethodsPrefix)
                    add(JLabel("Prefix for builder methods (ex. with)"))
                }
            )
        )
    }
}

data class OptionEntry(val property: OptionProperty, val type: OptionType, val component: JComponent)
