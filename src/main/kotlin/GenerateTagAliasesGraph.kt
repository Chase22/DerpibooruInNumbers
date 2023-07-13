import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeLines

data class Tag(val id: Long, val name: String, val slug: String) {
    fun toNameEntry(): String = "${id}[\"${StringEscapeUtils.escapeHtml4(name)}\"]"
}

fun main() {
    val resultSet = connection.createStatement().executeQuery("SELECT * from tag_aliases_view")
    val columns = resultSet.toMapSequence().toList()
    val idNames =
        (columns.map {
            Tag(it["tag_id"] as Long, it["tag_name"] as String,  it["tag_slug"] as String)
        } + columns.map {
            Tag(it["target_id"] as Long, it["target_name"] as String,  it["target_slug"] as String)
        }).associateBy { it.id }

    val groups = columns.groupBy { it["target_id"] as Long }
        .mapValues { entry -> entry.value.map { Tag(it["tag_id"] as Long, it["tag_name"] as String, it["tag_slug"] as String) } }
        .mapKeys { idNames[it.key]!! }

    val directory = File("diagrams")
    directory.listFiles()!!.forEach { it.delete() }

    groups.forEach { entry ->
        File(directory, "${entry.key.slug}.mmd").toPath().writeLines(
            listOf("flowchart TD") +
                    entry.key.toNameEntry()
                    + entry.value.map(Tag::toNameEntry)
                    + entry.value.map { "${it.id} --> ${entry.key.id}" }
        )
    }

    File(dataDir, "aliases.csv").toPath().writeLines(
        groups.entries.map { (listOf(it.key.name) + it.value.map(Tag::name)).joinToString() }
    ).also { println(it.absolutePathString()) }
}