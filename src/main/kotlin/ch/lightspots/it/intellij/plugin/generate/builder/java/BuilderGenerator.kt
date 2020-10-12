package ch.lightspots.it.intellij.plugin.generate.builder.java

import ch.lightspots.it.intellij.plugin.generate.builder.ext.sameAs
import com.intellij.codeInsight.generation.PsiFieldMember
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.PsiUtil

class BuilderGenerator(
    private val project: Project,
    private val file: PsiFile,
    private val editor: Editor,
    private val targetClazz: PsiClass,
    private val selectedFields: List<PsiFieldMember>
) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    fun generate() {
        val builderClazz = findOrCreateBuilderClass()
        val ctor = createConstructor(builderClazz)

        addMethod(targetClazz, ctor, replace = true)

        val staticBuilderMethod = createStaticBuilderMethod(builderClazz)
        addMethod(targetClazz, staticBuilderMethod)

        val builderCtor = createBuilderConstructor(builderClazz)
        addMethod(builderClazz, builderCtor)

        val buildMethod = createBuildMethod()
        // TODO add as last
        addMethod(builderClazz, buildMethod, replace = true)

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(file)
        CodeStyleManager.getInstance(project).reformat(builderClazz)
    }

    private fun findOrCreateBuilderClass(): PsiClass =
        targetClazz.findInnerClassByName(Constants.BUILDER_CLASS_NAME, false) ?: createBuilderClass()

    private fun createBuilderClass(): PsiClass {
        val builderClazz = psiElementFactory.createClass(Constants.BUILDER_CLASS_NAME)
        PsiUtil.setModifierProperty(builderClazz, PsiModifier.STATIC, true)
        PsiUtil.setModifierProperty(builderClazz, PsiModifier.FINAL, true)

        return targetClazz.add(builderClazz) as PsiClass
    }

    private fun createConstructor(builderClazz: PsiClass): PsiMethod {
        val ctor = psiElementFactory.createConstructor(targetClazz.name!!)
        // set constructor private
        ctor.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)

        ctor.parameterList.add(psiElementFactory.createParameter("builder", psiElementFactory.createType(builderClazz)))

        // FEATURE support for requireNonNull for NotNull fields
        selectedFields.forEach { member ->
            val field = member.element

            // search for setter for that field
            val setter = targetClazz.findMethodBySignature(PropertyUtil.generateSetterPrototype(field), true)

            val isFinal = field.modifierList?.hasModifierProperty(PsiModifier.FINAL) ?: false

            val text = if (setter == null || isFinal) {
                "${field.name} = builder.${field.name};"
            } else {
                "${setter.name}(builder.${field.name});"
            }

            ctor.body?.add(psiElementFactory.createStatementFromText(text, ctor))
        }
        return ctor
    }

    private fun createStaticBuilderMethod(builderClazz: PsiClass): PsiMethod {
        val builderType = psiElementFactory.createType(builderClazz)
        val method = psiElementFactory.createMethod("builder", builderType)
        PsiUtil.setModifierProperty(method, PsiModifier.STATIC, true)
        PsiUtil.setModifierProperty(method, PsiModifier.PUBLIC, true)

        // FEATURE Add final fields here

        val text = "return new ${builderType.presentableText}();"
        method.body?.add(psiElementFactory.createStatementFromText(text, method))

        return method
    }

    private fun createBuilderConstructor(builderClazz: PsiClass): PsiMethod {
        val ctor = psiElementFactory.createConstructor(builderClazz.name!!)
        // set constructor private
        // TODO make public if no static builder method is generated
        ctor.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)

        // FEATURE Add final fields here
        // ctor.parameterList
        //   .add(psiElementFactory.createParameter("builder", psiElementFactory.createType(builderClazz)))

        ctor.body?.add(psiElementFactory.createCommentFromText("// Use static builder() method", ctor))

        return ctor
    }

    private fun createBuildMethod(): PsiMethod {
        val buildMethod = psiElementFactory.createMethod("build", psiElementFactory.createType(targetClazz))

        PsiUtil.setModifierProperty(buildMethod, PsiModifier.PUBLIC, true)

        val text = "return new ${targetClazz.name}(this);"
        buildMethod.body?.add(psiElementFactory.createStatementFromText(text, buildMethod))

        return buildMethod
    }

    private fun addMethod(
        target: PsiClass,
        method: PsiMethod,
        replace: Boolean = false,
        after: PsiElement? = null
    ): PsiElement {
        var oldMethod = target.findMethodBySignature(method, false)
        if (oldMethod == null && method.isConstructor) {
            // search for existing constructor
            val ctor = target.constructors.find { it.parameterList sameAs method.parameterList }
            if (ctor != null) {
                oldMethod = ctor
            }
        }

        return if (oldMethod == null) {
            // add a new method
            if (after != null) {
                target.addAfter(method, after)
            } else {
                target.add(method)
            }
        } else if (replace) {
            // replace method
            oldMethod.replace(method)
        } else {
            oldMethod
        }
    }
}
