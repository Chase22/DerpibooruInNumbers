import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class GroupedTaggingHolder {
    val lock = ReentrantLock()

    fun getGroupedTaggings(): Map<Long, List<Long>>? {
        lock.lock()
        return (
                connection.createStatement().executeQuery("fetch 1000 from tag_cur").toMapSequence()
                    .map { it["image_id"] as Long to it["tag_id"] as Long }.toList()
                    .groupBy { it.first }
                    .mapValues { it.value.map(Pair<Long, Long>::second) }
                    .ifEmpty { null }
                ).also { lock.unlock() }
    }
}

fun main() {
    connection.createStatement().execute("drop table if exists normalized_taggings")
    connection.createStatement().execute("create table normalized_taggings(like image_taggings including all)")

    connection.createStatement().execute("BEGIN;")

    val df = DecimalFormat("#.###").apply { roundingMode = RoundingMode.DOWN }

    val insertStatement = connection.prepareStatement("insert into normalized_taggings(image_id, tag_id) values (?, ?)")

    val taggingCount = connection.createStatement()
        .executeQuery("select count(1) as count from image_taggings").apply { next() }.getObject(1) as Long

    var processed = 0

    try {
        println("fetch aliases")
        val tagAliases = connection.createStatement().executeQuery("select * from tag_aliases").toMapSequence()
            .map { it["tag_id"] as Long to it["target_tag_id"] as Long }.toMap()

        println("fetch taggings")
        connection.createStatement().execute("DECLARE tag_cur cursor for select * from image_taggings;")

        var processed = 0
        val taggingHolder = GroupedTaggingHolder()

        val executorService = Executors.newCachedThreadPool()

        repeat(2) {
            executorService.submit {
                var next: Map<Long, List<Long>>? = taggingHolder.getGroupedTaggings()
                while (next != null) {
                    next.mapValues {
                        it.value.map { tag ->
                            tagAliases[tag] ?: tag
                        }.distinct()
                    }.forEach { entry ->
                        entry.value.forEach { tag ->
                            try {
                                insertStatement.setObject(1, entry.key)
                                insertStatement.setObject(2, tag)
                                insertStatement.executeUpdate()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    processed += 1000
                    println("Normalized $processed/$taggingCount: ${df.format((processed.toDouble() / taggingCount) * 100)}%")
                    next = taggingHolder.getGroupedTaggings()
                }
            }
        }
        executorService.awaitTermination(24, TimeUnit.HOURS)
    } finally {
        connection.createStatement().execute("COMMIT;")
    }
}