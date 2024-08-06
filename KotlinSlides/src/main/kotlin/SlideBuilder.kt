@SlidesDsl
class SlideBuilder {
    private val classes: MutableList<String> = mutableListOf()

    private lateinit var title: String
    private lateinit var content: String
    private var notes: String? = null

    fun title(title: String) {
        this.title = title
    }

    fun content(text: String) {
        this.content = text
    }

    fun content(block: ContentBuilder.() -> Unit) {
        this.content = ContentBuilder().apply(block).build()
    }

    fun notes(text: String) {
        this.notes = text
    }

    fun classes(vararg slideClass: String) {
        classes.addAll(slideClass)
    }

    fun build(): String {
        return StringBuilder().apply {
            classes.takeIf { it.isNotEmpty() }?.let {
                appendLine("class: ${classes.joinToString()}")
            }

            appendLine("# $title")
            appendLine()
            append(content)
            appendLine()
            notes?.let {
                appendLine("???")
                append(it)
                appendLine()
            }
            appendLine()
            appendLine("---")
        }.toString()
    }
}