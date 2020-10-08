package ch.lightspots.it.intellij.plugin.generate.builder.ext

import com.intellij.openapi.diagnostic.Logger

fun Logger.info(msg: () -> String) {
    info(msg())
}
