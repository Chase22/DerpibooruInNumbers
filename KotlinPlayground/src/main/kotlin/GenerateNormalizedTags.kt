import java.math.RoundingMode
import java.sql.PreparedStatement
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class GroupedTaggingHolder {
    val lock = ReentrantLock()

    val selectionStatement: PreparedStatement by lazy {
        connection.prepareStatement("select * from image_taggings where image_id = any(?)")
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
    connection.createStatement().execute("drop table if exists normalized_taggings")
    connection.createStatement().execute("create table normalized_taggings(like image_taggings including all)")

    connection.createStatement().execute("BEGIN;")

    val df = DecimalFormat("#.###").apply { roundingMode = RoundingMode.DOWN }

    val imageCount = connection.createStatement()
        .executeQuery("select count(1) as count from images").apply { next() }.getObject(1) as Long

    try {
        println("fetch aliases")
        val tagAliases = connection.createStatement().executeQuery("select * from tag_aliases").toMapSequence()
            .map { it["tag_id"] as Long to it["target_tag_id"] as Long }.toMap()

        println("fetch taggings")
        connection.createStatement().execute("DECLARE img_cur cursor for select id from images;")

        var processed = 0
        val taggingHolder = GroupedTaggingHolder()

        val executorService = Executors.newCachedThreadPool()

        repeat(20) {
            executorService.submit {
                val insertStatement = connection.prepareStatement("insert into normalized_taggings(image_id, tag_id) values (?, ?)")

                var next = taggingHolder.getImages()
                while (next.isNotEmpty()) {
                    val taggings = taggingHolder.getGroupedTaggings(next) ?: continue
                    taggings.mapValues {
                        it.value.map { tag ->
                            tagAliases[tag] ?: tag
                        }.distinct()
                    }.forEach { entry ->
                            entry.value.forEach { tag ->
                                try {
                                    insertStatement.clearParameters()
                                    insertStatement.setObject(1, entry.key)
                                    insertStatement.setObject(2, tag)
                                    insertStatement.addBatch()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                    }
                    insertStatement.executeBatch()
                    processed += next.size
                    println("Normalized $processed/$imageCount: ${df.format((processed.toDouble() / imageCount) * 100)}%")
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

@OptIn(ExperimentalTime::class)
fun <T> printMeasuredTime(tag: String, block: () -> T): T = measureTimedValue(block).let {
    println("$tag took ${it.duration.inWholeMilliseconds}ms")
    it.value
}