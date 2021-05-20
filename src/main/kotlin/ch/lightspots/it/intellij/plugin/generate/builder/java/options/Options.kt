package ch.lightspots.it.intellij.plugin.generate.builder.java.options

import ch.lightspots.it.intellij.plugin.generate.builder.ext.getBoolean
import ch.lightspots.it.intellij.plugin.generate.builder.ext.getValue
import ch.lightspots.it.intellij.plugin.generate.builder.ext.setValue
import com.intellij.ide.util.PropertiesComponent
import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object Options {
    private const val columns = 10
    private val propertiesComponent = PropertiesComponent.getInstance()
    val availableOptions: List<OptionEntry>

    init {
        val staticBuilderNameTextField = textField(OptionProperty.STATIC_BUILDER_METHOD_NAME, "builder") {
            isEnabled = propertiesComponent.getBoolean(OptionProperty.STATIC_BUILDER_METHOD)
        }
        val staticBuilderMethodCheckBox = JCheckBox("Generate static builder method").apply {
            isSelected = propertiesComponent.getBoolean(OptionProperty.STATIC_BUILDER_METHOD)
            addActionListener {
                propertiesComponent.setValue(OptionProperty.STATIC_BUILDER_METHOD, isSelected)
                staticBuilderNameTextField.isEnabled = isSelected
            }
        }

        availableOptions = listOf(
            OptionEntry(
                OptionProperty.STATIC_BUILDER_METHOD,
                OptionType.CHECKBOX,
                optionLine(staticBuilderMethodCheckBox)
            ),
            OptionEntry(
                OptionProperty.STATIC_BUILDER_METHOD_NAME,
                OptionType.TEXT,
                optionLine(staticBuilderNameTextField, JLabel("Name of static builder method"))
            ),
            OptionEntry(
                OptionProperty.BUILDER_METHOD_PREFIX,
                OptionType.TEXT,
                labeledTextField(OptionProperty.BUILDER_METHOD_PREFIX, "", "Prefix for builder methods (ex. with)")
            ),
            OptionEntry(
                OptionProperty.NONNULL_ANNOTATION_NAME,
                OptionType.TEXT,
                labeledTextField(
                    OptionProperty.NONNULL_ANNOTATION_NAME,
                    "",
                    "Full qualified Name to @NonNull Annotation"
                )
            ),
            OptionEntry(
                OptionProperty.NULLABLE_ANNOTATION_NAME,
                OptionType.TEXT,
                labeledTextField(
                    OptionProperty.NULLABLE_ANNOTATION_NAME,
                    "",
                    "Full qualified Name to @Nullable Annotation"
                )
            )
        )
    }

    private fun optionLine(vararg elements: JComponent): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            elements.forEach { add(it) }
        }
    }

    private fun labeledTextField(optionProperty: OptionProperty, defaultValue: String, label: String): JPanel {
        return labeledTextField(optionProperty, defaultValue, label) {}
    }

    private fun labeledTextField(
        optionProperty: OptionProperty,
        defaultValue: String,
        label: String,
        textFieldApply: JTextField.() -> Unit
    ): JPanel {
        return optionLine(textField(optionProperty, defaultValue, textFieldApply), JLabel(label))
    }

    private fun textField(
        optionProperty: OptionProperty,
        defaultValue: String,
        apply: JTextField.() -> Unit
    ): JTextField {
        val textField = JTextField(defaultValue, columns).apply {
            propertiesComponent.getValue(optionProperty)?.let {
                text = it
            }
            val updateProperty = {
                propertiesComponent.setValue(optionProperty, text)
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
        textField.apply()
        return textField
    }
}

data class OptionEntry(val property: OptionProperty, val type: OptionType, val component: JComponent)
