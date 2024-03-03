package de.chasenet.derpibooru

import io.javalin.Javalin
import io.javalin.http.HttpStatus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import mu.KotlinLogging
import java.io.File
import java.io.FileFilter
import java.net.URL

private val cachingDir = File("imageCache").also { it.mkdirs() }

private val logger = KotlinLogging.logger {}
val json = Json {
    ignoreUnknownKeys = true

}

fun main() {
    Javalin.create().get("/{id}") { ctx ->
        val id = ctx.pathParam("id").ifBlank { null }?.toLongOrNull() ?: run {
            ctx.status(HttpStatus.BAD_REQUEST).result("id must be numeric")
            return@get
        }

        (getFromFile(id) ?: getFromDerpibooru(id))?.also {
            ctx.status(HttpStatus.OK).header("Content-type", it.second).result(it.first.readBytes())
            return@get
        }

        ctx.status(HttpStatus.NOT_FOUND).result("No file for $id found")
    }.start()
}

private fun getFromFile(id: Long): Pair<File, String>? {
    val files = cachingDir.listFiles(FileFilter { it.nameWithoutExtension.substringBefore(".") == id.toString() })
    if ((files?.size ?: 0) > 1) logger.warn { "More than one file for id $id" }

    return files?.firstOrNull()?.let {
        it to it.nameWithoutExtension.substringAfter(".").replace('_', '/')
    }?.also {
        logger.info { "Found file for id $id: ${it.first.name}" }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun getFromDerpibooru(id: Long): Pair<File, String>? {
    val image: DerpibooruImage = json.decodeFromStream<DerpibooruResponse>(
        URL(
            "https://derpibooru.org/api/v1/json/images/$id"
        ).openStream()
    ).image.takeIf { it.viewUrl != null && it.mimeType != null && it.name != null && it.format != null } ?: return null

    val file = File(cachingDir, "$id.${image.mimeType!!.replace('/', '_')}.${image.format}")

    file.delete()
    URL(image.representations["large"]!!).openStream().copyTo(file.outputStream())
    logger.info { "Getting image from derpibooru for id $id" }
    return file to image.mimeType
}

@Serializable
data class DerpibooruResponse(
    val image: DerpibooruImage
)

@Serializable
data class DerpibooruImage(
    @SerialName("name") val name: String? = null,
    @SerialName("view_url") val viewUrl: String? = null,
    @SerialName("mime_type") val mimeType: String? = null,
    @SerialName("format") val format: String? = null,
    @SerialName("duplicate_of") val duplicateOf: Int? = null,
    @SerialName("deletion_reason") val deletionReason: String? = null,
    @SerialName("representations") val representations: Map<String, String> = emptyMap()
)