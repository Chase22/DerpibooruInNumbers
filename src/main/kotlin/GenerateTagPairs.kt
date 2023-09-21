import java.math.RoundingMode
import java.sql.PreparedStatement
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class TaggingHolder {
    val lock = ReentrantLock()

    val selectionStatement: PreparedStatement by lazy {
        connection.prepareStatement("select * from normalized_taggings where image_id = any(?)")
    }

    fun getImages(): List<Long> {
        lock.lock()
        return (
                connection.createStatement()
                    .executeQuery("fetch 1000 from img_cur").toMapSequence().map { it["id"] as Long }.toList()
                ).also { lock.unlock() }
    }

    fun getGroupedTaggings(imageIds: List<Long>): Map<Long, List<Long>>? {
        return selectionStatement.apply { setArray(1, connection.createArrayOf("BIGINT", imageIds.toTypedArray())) }
            .executeQuery()
            .toMapSequence()
            .map { it["image_id"] as Long to it["tag_id"] as Long }.toList()
            .groupBy { it.first }
            .mapValues { it.value.map(Pair<Long, Long>::second) }
            .ifEmpty { null }
    }
}


fun main() {
    connection.createStatement().execute("drop table if exists tag_pairs")
    connection.createStatement().execute("create table tag_pairs ( image_id bigint, tag_id bigint, tag_id2 bigint)")

    connection.createStatement().execute("BEGIN;")

    val df = DecimalFormat("#.###").apply { roundingMode = RoundingMode.DOWN }

    val imageCount = connection.createStatement()
        .executeQuery("select count(1) as count from images").apply { next() }.getObject(1) as Long

    try {
        println("fetch taggings")
        connection.createStatement().execute("DECLARE img_cur cursor for select id from images;")

        var processed = 0
        val taggingHolder = TaggingHolder()

        val executorService = Executors.newCachedThreadPool()

        repeat(20) {
            executorService.submit {
                val insertStatement = connection.prepareStatement("insert into tag_pairs(image_id, tag_id, tag_id2) values (?, ?, ?)")

                var next = taggingHolder.getImages()
                while (next.isNotEmpty()) {
                    val taggings = taggingHolder.getGroupedTaggings(next) ?: continue
                    taggings.mapValues { (_, tags) ->
                        tags.flatMap { a ->
                            tags.mapNotNull { b ->
                                if (a == b) return@mapNotNull null
                                listOf(a,b).sorted().let { it.first() to it.last() }
                            }
                        }.distinct()
                    }.forEach { entry ->
                        entry.value.forEach { tag ->
                            try {
                                insertStatement.clearParameters()
                                insertStatement.setObject(1, entry.key)
                                insertStatement.setObject(2, tag.first)
                                insertStatement.setObject(3, tag.second)
                                insertStatement.addBatch()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    insertStatement.executeBatch()
                    processed += next.size
                    println("Paired $processed/$imageCount: ${df.format((processed.toDouble() / imageCount) * 100)}%")
                    next = taggingHolder.getImages()
                }
            }
        }

        executorService.shutdown()
        executorService.awaitTermination(24, TimeUnit.HOURS)
    } finally {
        connection.createStatement().execute("COMMIT;")
    }
}