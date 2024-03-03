package de.chasenet.derpibooru

import java.net.URL
import javax.imageio.ImageIO
import kotlin.io.path.*

data class SizeResult(
    val id: Int,
    val representation: Representation,
    val width: Int,
    val height: Int,
)

fun main() {
    val representations = ids.mapNotNull { id ->
        getRepresentationsFromDerpibooru(id).also {
            Thread.sleep(100)
        }?.let {
            id to it
        }
    }.toMap()

    var current = 0
    val size = representations.size
    val results = representations.flatMap { (id, currentRepresentations) ->
        println("$current/$size")
        current += 1
        Thread.sleep(100)
        currentRepresentations.map { (representation, url) ->
            val image = ImageIO.read(url)

            return@map SizeResult(
                id, representation, image.width, image.height
            )
        }
    }

    Path("output.csv").apply {
        deleteIfExists()
        createFile()
    }.writeLines(
        results.map { listOf(it.id, it.representation, it.width, it.height).joinToString() }
    )
}

enum class Representation {
    full,
    large,
    medium,
    small,
    tall,
    thumb,
    thumb_small,
    thumb_tiny,
}

fun getRepresentationsFromDerpibooru(imageId: Int): Map<Representation, URL>? {
    println(imageId)
    val image = URL("https://derpibooru.org/api/v1/json/images/$imageId").readText().let {
        json.decodeFromString<DerpibooruResponse>(it)
    }.image
    if (image.deletionReason != null) {
        println("image $imageId is deleted")
        return null
    }
    if (image.duplicateOf != null) {
        return getRepresentationsFromDerpibooru(image.duplicateOf)
    }
    if (listOf("image/png", "image/jpeg").contains(image.mimeType).not()) {
        println("Id $imageId is not an image")
        return null
    }

    return image.representations.entries.associate {
        Representation.valueOf(it.key) to URL(it.value)
    }
}