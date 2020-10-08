package ch.lightspots.it.intellij.plugin.generate.builder.actions

import ch.lightspots.it.intellij.plugin.generate.builder.java.GenerateBuilderActionHandler
import com.intellij.codeInsight.generation.actions.BaseGenerateAction

class BuilderAction : BaseGenerateAction(GenerateBuilderActionHandler())
