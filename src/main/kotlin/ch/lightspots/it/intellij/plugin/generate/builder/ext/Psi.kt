package ch.lightspots.it.intellij.plugin.generate.builder.ext

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.PsiParameterList
import com.intellij.psi.PsiType
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.PsiUtil

infix fun PsiParameterList.sameAs(other: PsiParameterList): Boolean {
    if (this.parametersCount != other.parametersCount) {
        return false
    }

    for (i in this.parameters.indices) {
        val param = this.parameters[i].type
        val otherParam = other.parameters[i].type

        if (!param.canonicalEqual(otherParam)) {
            return false
        }
    }
    return true
}

fun PsiType?.canonicalEqual(other: PsiType?): Boolean {
    return if (this != null && other != null) {
        this.presentableText.removePrefix("java.lang.") == other.presentableText.removePrefix("java.lang.")
    } else {
        false
    }
}

fun PsiModifierListOwner.modPublic() = PsiUtil.setModifierProperty(this, PsiModifier.PUBLIC, true)
fun PsiModifierListOwner.modPrivate() = PsiUtil.setModifierProperty(this, PsiModifier.PRIVATE, true)
fun PsiModifierListOwner.modStatic() = PsiUtil.setModifierProperty(this, PsiModifier.STATIC, true)
fun PsiModifierListOwner.modFinal() = PsiUtil.setModifierProperty(this, PsiModifier.FINAL, true)

fun PsiModifierListOwner.addAnnotation(qualifiedName: String) = this.modifierList?.addAnnotation(qualifiedName)

fun PsiClass.findSetterForField(field: PsiField) =
    findMethodBySignature(PropertyUtil.generateSetterPrototype(field), true)
fun PsiClass.findGetterForField(field: PsiField) =
    findMethodBySignature(PropertyUtil.generateGetterPrototype(field), true)
