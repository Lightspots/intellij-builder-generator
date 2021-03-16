package ch.lightspots.it.intellij.plugin.generate.builder.ext

import com.intellij.openapi.diagnostic.Logger

inline fun Logger.trace(msg: () -> String) {
    if (isTraceEnabled) {
        trace(msg())
    }
}

inline fun Logger.debug(msg: () -> String) {
    if (isDebugEnabled) {
        debug(msg())
    }
}

inline fun Logger.info(msg: () -> String) {
    info(msg())
}
