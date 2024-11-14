package uk.co.sentinelweb.cuer.hub.util.sleep

import java.io.BufferedReader
import java.io.InputStreamReader

class SleepPreventerLinuxDBus : SleepPreventer {
    private var inhibitCookie: String? = null

    //dbus-send --session --print-reply=literal --dest=org.freedesktop.PowerManager /org/freedesktop/PowerManager org.freedesktop.PowerManager.Inhibit string:MyApp string:Inhibit screensaver
    override fun preventSleep() {
        try {
            // Inhibit screensaver via dbus-send
            val pb = ProcessBuilder(
                "dbus-send", "--session",
                "--print-reply",
                "--dest=org.freedesktop.ScreenSaver",
                "/ScreenSaver",
                "org.freedesktop.ScreenSaver.Inhibit",
                "string:Cuer",
                "string:Inhibiting for demonstration"
            )
            pb.redirectErrorStream(true)
            val process = pb.start()

            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                // Read the response from dbus-send which contains the inhibit cookie
                var response: String
                while ((reader.readLine().also { response = it }) != null) {
                    if (response.contains("uint32")) {
                        inhibitCookie =
                            response.split("uint32\\s+".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].trim { it <= ' ' }
                        println("Inhibit Cookie: " + inhibitCookie)
                    }
                }
            }
            // Optionally wait for the process to complete or handle as needed
            process.waitFor()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun allowSleep() {
        if (inhibitCookie.isNullOrEmpty()) {
            System.err.println("Inhibit cookie is null or empty. Cannot allow screen sleep.")
            return
        }

        try {
            // Uninhibit screensaver via dbus-send using the stored inhibit cookie
            val pb = ProcessBuilder(
                "dbus-send", "--session",
                "--dest=org.freedesktop.ScreenSaver",
                "/ScreenSaver",
                "org.freedesktop.ScreenSaver.UnInhibit",
                "uint32:" + inhibitCookie
            )
            pb.inheritIO()
            val process = pb.start()

            // Optionally wait for the process to complete or handle as needed
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}