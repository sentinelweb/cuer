package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.ui.graphics.Color.Companion.White
import loadSVG
import java.awt.*

fun checkSystemTraySupport(): Boolean {
    return SystemTray.isSupported()
}

fun createTrayIconSimple() {
    if (!SystemTray.isSupported()) {
        println("System tray is not supported")
        return
    }

    try {
        // Load an image for the tray icon
        val trayIconImage = loadSVG("drawable/ic_launcher_yin_yang.svg", White, 24)

        //val trayIconImage = ImageIO.read(File("path/to/icon.png"))
        val trayIcon = TrayIcon(trayIconImage, "Cuer")

        // Set the auto-size option to true
        trayIcon.isImageAutoSize = true

        // Create a popup menu
        val popup = PopupMenu()

        // Create menu items
        val openItem = MenuItem("Open")
        val exitItem = MenuItem("Exit")

        // Add action listeners to the menu items
        openItem.addActionListener {
            println("Open option selected")
        }

        exitItem.addActionListener {
            System.exit(0)
        }

        // Add items to the popup menu
        popup.add(openItem)
        popup.add(exitItem)

        // Assign popup menu to the tray icon
        trayIcon.popupMenu = popup

        // Get the system tray instance
        val systemTray = SystemTray.getSystemTray()

        // Add the tray icon to the system tray
        systemTray.add(trayIcon)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
