import java.io.File

val dataDir: File by lazy {
    File("slides/data").apply { mkdirs() }
}

fun main() {
    val sourceDomains = connection.createStatement().executeQuery("select * from image_sources")
        .toMapSequence()
        .map { it["source"] as String }
        .map { it.ifBlank { null } }
        .map {it?.substringAfter("://")?.substringBefore("/") }
        .groupingBy { it }.eachCount()
        .toList().let {
            val tumblr = it.filter { it.first?.endsWith("tumblr.com") ?: false }.sumOf { it.second }
            val deviantArt = it.filter { it.first?.endsWith("deviantart.com") ?: false }.sumOf { it.second }
            listOf("tumblr" to tumblr, "deviantart" to deviantArt) + it
        }
        .filter { it.first?.endsWith("tumblr.com") == false }
        .filter { it.first?.endsWith("deviantart.com") == false }

    val outputFile = File(dataDir, "sources.csv")
    outputFile.writeText(sourceDomains.joinToString(System.lineSeparator()) { "${it.first ?: ""}, ${it.second}" })
}