package ch.lightspots.it.intellij.plugin.generate.builder.ext

import ch.lightspots.it.intellij.plugin.generate.builder.java.options.OptionProperty
import com.intellij.ide.util.PropertiesComponent

fun PropertiesComponent.getValue(option: OptionProperty): String? = getValue(option.name)
fun PropertiesComponent.getBoolean(option: OptionProperty): Boolean = getBoolean(option.name)

fun PropertiesComponent.setValue(option: OptionProperty, value: String?) = setValue(option.name, value)
fun PropertiesComponent.setValue(option: OptionProperty, value: Boolean) = setValue(option.name, value)
