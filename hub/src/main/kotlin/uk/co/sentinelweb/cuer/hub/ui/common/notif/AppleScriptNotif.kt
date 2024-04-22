package uk.co.sentinelweb.cuer.hub.ui.common.notif

class AppleScriptNotif {
    companion object {
        fun showNotification(title: String, text: String) {
            val script = """
        display notification "$text" with title "$title"
    """.trimIndent()

            val command = arrayOf("/usr/bin/osascript", "-e", script)

            val process = ProcessBuilder(*command).start()
            process.waitFor()
        }
    }
}