package uk.co.sentinelweb.cuer.hub.ui.filebrowser.viewer

import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.awt.Desktop
import java.io.File

fun openFileInDefaultApp(media: MediaDomain) {
    val file = File(media.platformId)

    // Ensure the file exists
    if (!file.exists()) {
        println("Could not find file: ${media.platformId}")
        return
    }

    // Ensure Desktop API is supported
    if (!Desktop.isDesktopSupported()) {
        println("Desktop operations not supported")
        return
    }

    val desktop = Desktop.getDesktop()

    // Ensure this desktop instance can open files
    if (!desktop.isSupported(Desktop.Action.OPEN)) {
        println("Desktop doesn't support the 'open' action")
        return
    }

    // Open the file
    try {
        desktop.open(file)
    } catch (e: Exception) {
        println("Error opening file: ${e.message}")
    }
}