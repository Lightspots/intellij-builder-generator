package ch.lightspots.it.intellij.plugin.generate.builder.java.options

enum class OptionProperty {
    STATIC_BUILDER_METHOD,
    STATIC_BUILDER_METHOD_NAME,
    BUILDER_METHOD_PREFIX,
    NULLABLE_ANNOTATION_NAME,
    NONNULL_ANNOTATION_NAME,
    REQUIRE_NONNULL,
    COPY_BUILDER;

    private val prefix: String = "ch.lightspots.it.intellij.plugin.generate.builder"

    val string: String
        get() = "$prefix.${name.lowercase()}"
}
