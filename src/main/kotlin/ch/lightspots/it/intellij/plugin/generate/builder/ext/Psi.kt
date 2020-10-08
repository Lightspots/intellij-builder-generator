package ch.lightspots.it.intellij.plugin.generate.builder.ext

import com.intellij.psi.PsiParameterList
import com.intellij.psi.PsiType

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
