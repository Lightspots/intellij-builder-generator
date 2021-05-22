package ch.lightspots.it.intellij.plugin.generate.builder.java

import ch.lightspots.it.intellij.plugin.generate.builder.ext.addAnnotation
import ch.lightspots.it.intellij.plugin.generate.builder.ext.canonicalEqual
import ch.lightspots.it.intellij.plugin.generate.builder.ext.findGetterForField
import ch.lightspots.it.intellij.plugin.generate.builder.ext.findSetterForField
import ch.lightspots.it.intellij.plugin.generate.builder.ext.getBoolean
import ch.lightspots.it.intellij.plugin.generate.builder.ext.getValue
import ch.lightspots.it.intellij.plugin.generate.builder.ext.modFinal
import ch.lightspots.it.intellij.plugin.generate.builder.ext.modPrivate
import ch.lightspots.it.intellij.plugin.generate.builder.ext.modPublic
import ch.lightspots.it.intellij.plugin.generate.builder.ext.modStatic
import ch.lightspots.it.intellij.plugin.generate.builder.ext.sameAs
import ch.lightspots.it.intellij.plugin.generate.builder.java.options.OptionProperty
import com.intellij.codeInsight.generation.PsiFieldMember
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager

class BuilderGenerator(
    private val project: Project,
    private val file: PsiFile,
    private val editor: Editor,
    private val targetClazz: PsiClass,
    private val selectedFields: List<PsiFieldMember>
) {
    private val psiElementFactory: PsiElementFactory = JavaPsiFacade.getInstance(project).elementFactory
    private val builderType: PsiType = psiElementFactory.createTypeFromText(Constants.BUILDER_CLASS_NAME, null)
    private val propertiesComponent = PropertiesComponent.getInstance()

    private val nonNullAnnotation: String?
    private val nullableAnnotation: String?

    init {
        val nonNullVal = propertiesComponent.getValue(OptionProperty.NONNULL_ANNOTATION_NAME)
        nonNullAnnotation = if (nonNullVal.isNullOrBlank()) {
            null
        } else {
            nonNullVal.trim()
        }
        val nullableVal = propertiesComponent.getValue(OptionProperty.NULLABLE_ANNOTATION_NAME)
        nullableAnnotation = if (nullableVal.isNullOrBlank()) {
            null
        } else {
            nullableVal.trim()
        }
    }

    fun generate() {
        val builderClazz = findOrCreateBuilderClass()

        val ctor = createConstructor()
        targetClazz.addMethod(ctor, replace = true)

        var lastAddedMember: PsiElement? = null
        for (member in selectedFields) {
            lastAddedMember = builderClazz.findOrCreateField(member, lastAddedMember)
        }

        val lastFieldInClass = targetClazz.fields.last()

        val useStaticBuilderMethod = propertiesComponent.getBoolean(OptionProperty.STATIC_BUILDER_METHOD)
        if (useStaticBuilderMethod) {
            val staticBuilderMethod = createStaticBuilderMethod()
            targetClazz.addMethod(staticBuilderMethod, after = lastFieldInClass)
        }

        val builderCtor = createBuilderConstructor(builderClazz, useStaticBuilderMethod)
        lastAddedMember = builderClazz.addMethod(builderCtor, after = lastAddedMember)

        val methodPrefix = propertiesComponent.getValue(OptionProperty.BUILDER_METHOD_PREFIX) ?: ""
        for (member in selectedFields) {
            val method = createMethodForField(member, methodPrefix)
            lastAddedMember = builderClazz.addMethod(method, after = lastAddedMember)
        }

        val buildMethod = createBuildMethod()
        builderClazz.addMethod(buildMethod, after = lastAddedMember, replace = true)

        JavaCodeStyleManager.getInstance(project).shortenClassReferences(file)
        CodeStyleManager.getInstance(project).reformat(file)
    }

    private fun findOrCreateBuilderClass(): PsiClass =
        targetClazz.findInnerClassByName(Constants.BUILDER_CLASS_NAME, false) ?: createBuilderClass()

    private fun createBuilderClass(): PsiClass {
        val builderClazz = psiElementFactory.createClass(Constants.BUILDER_CLASS_NAME)
        builderClazz.modStatic()
        builderClazz.modFinal()

        return targetClazz.add(builderClazz) as PsiClass
    }

    private fun PsiClass.findOrCreateField(member: PsiFieldMember, last: PsiElement? = null): PsiElement {
        val field = member.element

        val oldField = this.findFieldByName(field.name, false)
        if (oldField == null || !field.type.canonicalEqual(oldField.type)) {
            // remove oldField
            oldField?.delete()

            val newField = psiElementFactory.createField(field.name, field.type)

            return if (last != null) {
                addAfter(newField, last)
            } else {
                add(newField)
            }
        }
        return oldField
    }

    private fun createMethodForField(member: PsiFieldMember, prefix: String): PsiMethod {
        val field = member.element

        val methodName = if (prefix.isBlank()) {
            field.name
        } else {
            val capitalizedName = field.name.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            "${prefix.trim()}$capitalizedName"
        }

        val parameterName = field.name

        val method = psiElementFactory.createMethod(methodName, builderType)
        method.modPublic()
        nonNullAnnotation?.let { method.addAnnotation(it) }

        val parameter = psiElementFactory.createParameter(parameterName, field.type)

        val getter = targetClazz.findGetterForField(field)
        if (nonNullAnnotation?.let { getter?.getAnnotation(it) != null || field.getAnnotation(it) != null } == true) {
            parameter.addAnnotation(nonNullAnnotation)
        } else if (
            nullableAnnotation?.let {
                getter?.getAnnotation(it) != null || field.getAnnotation(it) != null
            } == true
        ) {
            parameter.addAnnotation(nullableAnnotation)
        }
        method.parameterList.add(parameter)

        val assignText = "this.${field.name} = ${field.name};"
        val returnText = "return this;"
        method.body?.add(psiElementFactory.createStatementFromText(assignText, method))
        method.body?.add(psiElementFactory.createStatementFromText(returnText, method))

        return method
    }

    private fun createConstructor(): PsiMethod {
        val ctor = psiElementFactory.createConstructor(targetClazz.name!!)
        // set constructor private
        ctor.modPrivate()

        ctor.parameterList.add(psiElementFactory.createParameter("builder", builderType))

        // FEATURE support for requireNonNull for NotNull fields
        selectedFields.forEach { member ->
            val field = member.element

            // search for setter for that field
            val setter = targetClazz.findSetterForField(field)

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

    private fun createStaticBuilderMethod(): PsiMethod {
        val methodName = propertiesComponent.getValue(OptionProperty.STATIC_BUILDER_METHOD_NAME) ?: "builder"
        val method = psiElementFactory.createMethod(methodName, builderType)
        method.modStatic()
        method.modPublic()
        nonNullAnnotation?.let { method.addAnnotation(it) }

        // FEATURE Add final fields here

        val text = "return new ${builderType.presentableText}();"
        method.body?.add(psiElementFactory.createStatementFromText(text, method))

        return method
    }

    private fun createBuilderConstructor(builderClazz: PsiClass, useStaticBuilderMethod: Boolean): PsiMethod {
        val ctor = psiElementFactory.createConstructor(builderClazz.name!!)

        if (useStaticBuilderMethod) {
            // set constructor private if static builder method is generated
            ctor.modPrivate()
            val methodName = propertiesComponent.getValue(OptionProperty.STATIC_BUILDER_METHOD_NAME) ?: "builder"
            ctor.body?.add(psiElementFactory.createCommentFromText("// Use static $methodName() method", ctor))
        } else {
            // set constructor public
            ctor.modPublic()
        }

        // FEATURE Add final fields here
        // ctor.parameterList
        //   .add(psiElementFactory.createParameter("builder", psiElementFactory.createType(builderClazz)))

        return ctor
    }

    private fun createBuildMethod(): PsiMethod {
        val buildMethod = psiElementFactory.createMethod("build", psiElementFactory.createType(targetClazz))
        buildMethod.modPublic()
        nonNullAnnotation?.let { buildMethod.addAnnotation(it) }

        val text = "return new ${targetClazz.name}(this);"
        buildMethod.body?.add(psiElementFactory.createStatementFromText(text, buildMethod))

        return buildMethod
    }

    private fun PsiClass.addMethod(
        method: PsiMethod,
        replace: Boolean = false,
        after: PsiElement? = null
    ): PsiElement {
        var oldMethod = findMethodBySignature(method, false)
        if (oldMethod == null && method.isConstructor) {
            // search for existing constructor
            val ctor = constructors.find { it.parameterList sameAs method.parameterList }
            if (ctor != null) {
                oldMethod = ctor
            }
        }

        return if (oldMethod == null) {
            // add a new method
            if (after != null) {
                addAfter(method, after)
            } else {
                add(method)
            }
        } else if (replace) {
            // replace method
            oldMethod.replace(method)
        } else {
            oldMethod
        }
    }
}
