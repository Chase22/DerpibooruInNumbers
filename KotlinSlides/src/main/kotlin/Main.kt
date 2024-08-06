import SlidesBuilder.Companion.slides

fun main() {
    slides {
        slide {
            classes("middle", "center")

            title("Derpibooru in Numbers")
            content {
                wrapped("image50") {
                    image("img/derpibooru.png", "derpibooru-logo")
                }
            }
        }
        slide {
            title("Content Warning")
            content {
                wrapped("center") {
                    line("This panel contains mention of not safe for work content and themes, as well as mild swearing")
                }
            }
            notes("Questions are fine during the talk")
        }
    }.let(::println)
}

@DslMarker
annotation class SlidesDsl

@SlidesDsl
class SlidesBuilder {
    private val output: Appendable = StringBuilder()

    fun slide(block: SlideBuilder.() -> Unit) {
        output.append(SlideBuilder().apply(block).build())
    }

    fun build(): String = output.toString()

    companion object {
        fun slides(block: SlidesBuilder.() -> Unit): String = SlidesBuilder().apply(block).build()
    }
}

fun Appendable.appendLines(amount: Int) {
    repeat(amount) { this.appendLine() }
}