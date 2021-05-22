package ch.lightspots.it.intellij.plugin.generate.builder.ext

import ch.lightspots.it.intellij.plugin.generate.builder.java.options.OptionProperty
import com.intellij.ide.util.PropertiesComponent

fun PropertiesComponent.getValue(option: OptionProperty): String? = getValue(option.string)
fun PropertiesComponent.getBoolean(option: OptionProperty): Boolean = getBoolean(option.string)

fun PropertiesComponent.setValue(option: OptionProperty, value: String?) = setValue(option.string, value)
fun PropertiesComponent.setValue(option: OptionProperty, value: Boolean) = setValue(option.string, value)
