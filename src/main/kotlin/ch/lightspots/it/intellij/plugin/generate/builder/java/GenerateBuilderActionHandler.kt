package ch.lightspots.it.intellij.plugin.generate.builder.java

import ch.lightspots.it.intellij.plugin.generate.builder.ext.debug
import ch.lightspots.it.intellij.plugin.generate.builder.ext.trace
import ch.lightspots.it.intellij.plugin.generate.builder.java.options.Options
import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.FileModificationService
import com.intellij.codeInsight.generation.PsiFieldMember
import com.intellij.ide.util.MemberChooser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiSubstitutor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.TypeConversionUtil

/**
 * @see org.jetbrains.java.generate.GenerateToStringActionHandlerImpl
 */
class GenerateBuilderActionHandler : CodeInsightActionHandler {
    companion object {
        private val logger = Logger.getInstance("#GenerateBuilderActionHandler")

        private val javaLoggers = listOf(
            "org.apache.log4j.Logger",
            "org.apache.logging.log4j.Logger",
            "java.util.logging.Logger",
            "org.slf4j.Logger",
            "ch.qos.logback.classic.Logger",
            "net.sf.microlog.core.Logger",
            "org.apache.commons.logging.Log",
            "org.pmw.tinylog.Logger",
            "org.jboss.logging.Logger",
            "jodd.log.Logger"
        )
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        logger.trace { "invoke on file $file" }
        val (clazz, element) = getSubjectClass(editor, file) ?: return

        if (!FileModificationService.getInstance().preparePsiElementsForWrite(clazz)) {
            return
        }

        logger.debug { "Current project ${project.name}" }

        val dialogMembers = buildMembersToShow(clazz, element)

        val options = Options.availableOptions.map { it.component }.toTypedArray()

        logger.debug("Displaying member chooser dialog")

        val chooser: MemberChooser<PsiFieldMember> = MemberChooser<PsiFieldMember>(
            dialogMembers,
            false,
            true,
            project,
            null,
            options
        )
        chooser.title = "Generate Builder"

        chooser.selectElements(getPreselection(clazz, dialogMembers))

        if (ApplicationManager.getApplication().isUnitTestMode) {
            chooser.close(DialogWrapper.OK_EXIT_CODE)
        } else {
            chooser.show()
        }
        if (DialogWrapper.OK_EXIT_CODE == chooser.exitCode) {
            val selectedMembers = chooser.selectedElements
            if (selectedMembers != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    BuilderGenerator(
                        project,
                        file,
                        editor,
                        clazz,
                        selectedMembers
                    ).generate()
                }
            }
        }

        logger.trace { "invoke finished" }
    }

    private fun getPreselection(
        clazz: PsiClass,
        dialogMembers: Array<PsiFieldMember>
    ): Array<PsiFieldMember> {
        return dialogMembers
            .filter { member ->
                member.element.containingClass === clazz
            }
            .toTypedArray()
    }

    private fun buildMembersToShow(clazz: PsiClass, element: PsiElement): Array<PsiFieldMember> {
        val list = mutableListOf<PsiFieldMember>()

        val helper = JavaPsiFacade.getInstance(clazz.project).resolveHelper

        var classToExtract: PsiClass? = clazz
        while (classToExtract != null) {
            val fields = clazz.fields
                .filter { psiField ->
                    helper.isAccessible(psiField, classToExtract!!, clazz)
                    // TODO ignore fields with setters
                }
                .filterNot { PsiTreeUtil.isAncestor(it, element, false) }
                // ignore static fields
                .filterNot { it.hasModifierProperty(PsiModifier.STATIC) }
                // ignore logging fields
                .filterNot { javaLoggers.contains(it.type.canonicalText) }
                // ignore final fields with initializer
                .filter { it.hasModifierProperty(PsiModifier.FINAL) && it.initializer == null }
                // ignore final fields on superclass
                .filter { it.hasModifierProperty(PsiModifier.FINAL) && clazz.isEquivalentTo(classToExtract) }
                .mapNotNull { psiField ->
                    val containingClass = psiField.containingClass
                    if (containingClass != null) {
                        PsiFieldMember(
                            psiField,
                            TypeConversionUtil.getSuperClassSubstitutor(
                                containingClass,
                                classToExtract!!,
                                PsiSubstitutor.EMPTY
                            )
                        )
                    } else {
                        null
                    }
                }

            list += fields
            classToExtract = classToExtract.superClass
        }
        return list.toTypedArray()
    }

    private fun getSubjectClass(editor: Editor, file: PsiFile?): Pair<PsiClass, PsiElement>? {
        if (file == null) {
            return null
        }
        val offset = editor.caretModel.offset
        val context = file.findElementAt(offset) ?: return null
        val clazz = PsiTreeUtil.getParentOfType(context, PsiClass::class.java, false) ?: return null

        return clazz to context
    }
}
