package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.ui.graphics.Color.Companion.White
import loadSVG
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import javax.imageio.ImageIO

fun createTrayIcon(popup: ComposePopup) {
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

        trayIcon.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                println("mouseClicked: popup.isVisible= ${popup.isVisible}")
                if (popup.isVisible) {
                    popup.hidePopup()
                } else {
                    popup.showPopup()
                }
            }

            override fun mousePressed(e: MouseEvent?) {
                println("mousePressed")
            }

            override fun mouseReleased(e: MouseEvent?) {
                println("mouseReleased")
            }

            override fun mouseEntered(e: MouseEvent?) {
                println("mouseEntered")
            }

            override fun mouseExited(e: MouseEvent?) {
                println("mouseExited")
            }

        })

        // Get the system tray instance
        val systemTray = SystemTray.getSystemTray()

        // Add the tray icon to the system tray
        systemTray.add(trayIcon)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun main() {
    javax.swing.SwingUtilities.invokeLater {
        val composePopup = ComposePopup()
        createTrayIcon(composePopup)
    }
}
