@SlidesDsl
class ContentBuilder {
    private val content = StringBuilder()

    fun line(text: String) {
        content.appendLine(text)
    }

    fun list(vararg entries: String) {
        content.appendLine(
            entries.joinToString(separator = "\n") { "- $it" }
        )
    }

    fun image(path: String, alternative: String) {
        content.append("![$alternative]($path)")
    }

    fun wrapped(clazz: String, block: ContentBuilder.() -> Unit) {
        content.appendLine(
            ".$clazz[${
                ContentBuilder().apply(block).build()
            }]"
        )
    }

    fun build(): String = content.toString()

}
