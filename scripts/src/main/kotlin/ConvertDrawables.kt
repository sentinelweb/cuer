import java.io.File
import kotlin.text.RegexOption.DOT_MATCHES_ALL

// runwith  ./gradlew scripts:convertDrawablesToSVG
fun main(args: Array<String>) {
    println("Start ")
    val path = "/Users/robmunro/repos/cuer"
    val inputDirPath =
        "$path/app/src/main/res/drawable" // Path to the directory containing the Android Vector drawable XML files
    val outputDirPath =
        "$path/hub/src/main/resources/drawable" // Path to the directory where the output SVG files will be saved

    val inputDir = File(inputDirPath)
    val outputDir = File(outputDirPath).apply { mkdirs() }
    println("dirs: in:${inputDir.toString()} out:${outputDir.toString()} ")
    inputDir
        .listFiles { _, name -> name.endsWith(".xml") }
        ?.also { println("Files: $it") }
        ?.forEach { file ->
            convertXmlToSvg(file, outputDir)
        }
}

val pathMapping = mapOf(
    "play_path" to "M 8 5 L 8 19 L 19 12 L 19 12 L 19 12 Z M 0 0 L 0 0 L 0 0 L 0 0 L 0 0 Z",
    "pause_path" to "M 6 19 L 10 19 L 10 5 L 6 5 L 6 19 Z M 14 5 L 14 19 L 18 19 L 18 5 L 14 5 Z"
)

val colorMapping = mapOf(
    "white" to "#FFFFFF"
)

val dimenMapping = mapOf(
    "menu_icon_size" to "24",
    "button_icon_size" to "24",
    "item_swipe_icon_size" to "24",
    "player_button_icon_size" to "48",
    "notif_button_icon_size" to "24",
    "pref_icon_size" to "24",
    "chip_icon_size" to "16",
    "player_fab_button_icon_size" to "64",
)

// todo use an xml parser!
// convert viewBox properly too android:viewportHeight="24" android:viewportWidth="24"
private fun convertXmlToSvg(xmlFile: File, outputDir: File) {
    val xmlContents = xmlFile.readText()   // read the whole file as a single string

    val fileName = xmlFile.nameWithoutExtension
    val svgFile = File(outputDir, "$fileName.svg")

    // Extract relevant properties
    val viewBox = Regex("""viewBox=['"](.*?)['"]""").find(xmlContents)?.groupValues?.get(1)
    val width = Regex("""android:width=['"](.*?)['"]""").find(xmlContents)?.groupValues?.get(1)
        ?.replace("dp", "")
        ?.replaceDimensions()
    val height = Regex("""android:height=['"](.*?)['"]""").find(xmlContents)?.groupValues?.get(1)
        ?.replace("dp", "")
        ?.replaceDimensions()

    // Extract all <path> elements
    val rawPaths =
        Regex("""<path.*?/>""", DOT_MATCHES_ALL)
            .findAll(xmlContents)
            .map { it.value }
            .map { it.replace("android:", "") }
            .map { it.replace("pathData", "d") }
            .map { it.replace("fillColor", "fill") }
            .map { it.replace(Regex("""name=".*?"\s*"""), "") }
            .map { it.replaceStringPaths() }
            .toList()

    println("${xmlFile.name} -> ")

    if (rawPaths.size > 0) {
        val processedPaths = rawPaths.map { path ->
            var newPath = path
            colorMapping.forEach { (colorName, hex) ->
                newPath = newPath.replace("fill=\"@color/$colorName\"", "fill=\"$hex\"")
            }
            newPath
        }

        // Prepare SVG file content
        val svgContentsBuilder = StringBuilder()

        svgContentsBuilder.append("<svg xmlns=\"http://www.w3.org/2000/svg\"")
        if (viewBox != null) svgContentsBuilder.append(" viewBox=\"$viewBox\"")
        if (width != null) svgContentsBuilder.append(" width=\"$width\"")
        if (height != null) svgContentsBuilder.append(" height=\"$height\"")
        svgContentsBuilder.append(">\n")

        processedPaths.forEach { svgContentsBuilder.append("\t$it\n") }

        svgContentsBuilder.append("</svg>")

        processedPaths.forEach { println("$it") }
        println(svgContentsBuilder.toString())

        // Write to SVG file
        svgFile.writeText(svgContentsBuilder.toString())
    }


}

private fun String.replaceDimensions(): String {
    var newAttr = this
    dimenMapping.forEach { (dimenName, value) ->
        newAttr = newAttr.replace("@dimen/$dimenName", value)
    }
    return newAttr
}

private fun String.replaceStringPaths(): String {
    var newAttr = this
    pathMapping.forEach { (dimenName, value) ->
        newAttr = newAttr.replace("@string/$dimenName", value)
    }
    return newAttr
}

//private fun convertXmlToSvg(xmlFile: File, outputDir: File) {
//    val lines = xmlFile.readLines()
//    val fileName = xmlFile.nameWithoutExtension
//
//    val svgLines = mutableListOf<String>()
//
//    svgLines += "<svg xmlns='http://www.w3.org/2000/svg'>"
//
//    lines
//        .filter { it.contains("<path") }
//        .also { println("${xmlFile.name} -> $it") }
//        .map { it.replace("android:", "") }
//        .forEach { svgLines += it }
//
//    svgLines += "</svg>"
//
//    File(outputDir, "$fileName.svg").writeText(svgLines.joinToString("\n"))
//}