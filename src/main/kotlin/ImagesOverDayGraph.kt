import jetbrains.datalore.plot.PlotHtmlExport
import jetbrains.datalore.plot.PlotHtmlHelper
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.tooltips.layerTooltips
import java.nio.file.Files
import java.sql.Timestamp
import java.time.ZoneOffset
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

fun main() {
    val data = connection.createStatement().executeQuery("select * from images_by_day_in_april").toMapSequence().map {
        it["day"] as Timestamp to it["count"] as Long
    }.toMap().mapKeys { it.key.toLocalDateTime().toEpochSecond(ZoneOffset.UTC) * 1000 }

    val graph = letsPlot(
        mapOf(
            "date" to data.keys.toList(),
            "count" to data.values.toList()
        )
    ) { x = "date"; y = "count"; } + geomLine(
        tooltips = layerTooltips()
            .format("date", "%b %d")
    ) + ylab("count") + scaleXDateTime(format="%b %d") + ggsize(2000, 1000)

    val html = PlotHtmlExport.buildHtmlFromRawSpecs(graph.toSpec(), PlotHtmlHelper.scriptUrl("3.2.0"), iFrame = true)
    val file = Files.createTempFile("plot", ".html").apply { writeText(html) }
    ProcessBuilder("open", file.absolutePathString()).start().waitFor()
}