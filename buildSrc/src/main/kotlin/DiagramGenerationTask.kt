import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.io.FileFilter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

fun generateDiagram(sourceFile: File, svgFile: File) {
    try {
//        println("Processing Diagram " + sourceFile.name + "...")
        ProcessBuilder().directory(sourceFile.parentFile)
            .command("npx --yes -p @mermaid-js/mermaid-cli mmdc -i \"${sourceFile.name}\" -o \"${svgFile.name}\"".split(" "))
            .inheritIO().start().waitFor()
    } catch (e: Exception) {
        println(e.message)
        throw RuntimeException(e)
    }
}

abstract class DiagramGenerationTask : DefaultTask() {
    @get:OutputDirectory
    abstract val destinationDirectory: DirectoryProperty

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @TaskAction
    fun createHashes() {
        println(destinationDirectory.get().asFile.absolutePath)
        val threadPool = Executors.newFixedThreadPool(50)
        val files = destinationDirectory.get().asFile.listFiles(FileFilter { it.name.endsWith(".mmd") })!!
        var processedDiagrams = 0

        for (sourceFile in files) {
            threadPool.submit {
                val svgFile: Provider<RegularFile> = destinationDirectory.file(sourceFile.name + ".svg")
                generateDiagram(sourceFile, svgFile.get().asFile)
                processedDiagrams++;
                if (processedDiagrams%10 == 0) {
                    println("Diagramms: $processedDiagrams/${files.size} ${((processedDiagrams.toFloat()/files.size)*100)}%")
                }
            }
        }
        threadPool.awaitTermination(10, TimeUnit.MINUTES)
    }
}