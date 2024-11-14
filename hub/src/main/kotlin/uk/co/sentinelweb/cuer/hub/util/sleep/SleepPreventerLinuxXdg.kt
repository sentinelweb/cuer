package uk.co.sentinelweb.cuer.hub.util.sleep

import uk.co.sentinelweb.cuer.hub.ui.player.vlc.WINDOW_NAME
import java.io.BufferedReader
import java.io.InputStreamReader

class SleepPreventerLinuxXdg : SleepPreventer {
    private val appWindowId: String? by lazy { getWindowId(WINDOW_NAME) }

    // Method to get the window ID
    fun getWindowId(windowName: String?): String? {
        var windowId: String? = null
        try {
            // Using xdotool to search for the window by its name
            val pb = ProcessBuilder("xdotool", "search", "--name", windowName)
            pb.redirectErrorStream(true)
            val process = pb.start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                windowId = reader.readLine().trim { it <= ' ' }
            }
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return windowId
    }

    // Method to prevent screen sleep
    override fun preventSleep() {
        if (appWindowId == null) {
            System.err.println("Window ID is null. Cannot prevent screen sleep.")
            return
        }
        try {
            // Suspend the screensaver for the specific window
            val pb = ProcessBuilder("xdg-screensaver", "suspend", appWindowId)
            pb.inheritIO()
            val process = pb.start()

            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Method to allow screen sleep
    override fun allowSleep() {
        if (appWindowId == null) {
            System.err.println("Window ID is null. Cannot allow screen sleep.")
            return
        }
        try {
            // Resume the screensaver for the specific window
            val pb = ProcessBuilder("xdg-screensaver", "resume", appWindowId)
            pb.inheritIO()
            val process = pb.start()

            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}