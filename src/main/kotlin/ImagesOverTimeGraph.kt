import jetbrains.datalore.base.geometry.DoubleVector
import jetbrains.datalore.plot.PlotSvgExport
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.tooltips.layerTooltips
import java.io.File
import java.sql.Timestamp
import java.time.ZoneOffset

fun main() {
    val data = connection.createStatement().executeQuery("select * from images_by_month").toMapSequence().map {
        it["month"] as Timestamp to it["count"] as Long
    }.toMap().mapKeys { it.key.toLocalDateTime().toEpochSecond(ZoneOffset.UTC) * 1000 }

    val graph = letsPlot(
        mapOf(
            "date" to data.keys.toList(),
            "count" to data.values.toList()
        )
    ) { x = "date"; y = "count"; } + geomLine(
        tooltips = layerTooltips()
            .format("date", "%B %Y")
    ) + ylab("count") + scaleXDateTime(format="%b %Y") + ggsize(2000, 1000)

    val svg = PlotSvgExport.buildSvgImageFromRawSpecs(graph.toSpec(), DoubleVector(1500.0, 1000.0))
    File(dataDir, "imagesOverTime.svg").writeText(svg)
}