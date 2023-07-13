import java.io.File

val dataDir: File by lazy {
    File("build/data").apply { mkdirs() }
}

fun main() {
    val sourceDomains = connection.createStatement().executeQuery("select * from image_sources")
        .toMapSequence()
        .map { it["source"] as String }
        .map { it.ifBlank { null } }
        .map { it?.substringAfter("://")?.substringBefore("/") }
        .groupingBy { it }.eachCount()
        .toList()

    val outputFile = File(dataDir, "sources.csv")
    outputFile.writeText(sourceDomains.joinToString(System.lineSeparator()) { "${it.first ?: ""}, ${it.second}" })
}