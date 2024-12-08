package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.ui.graphics.Color.Companion.White
import loadSVG
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.SwingUtilities

class SystemTrayIcon : KoinComponent {

    private val popup: SystemTrayComposePopup by inject()

    fun createTrayIcon() = SwingUtilities.invokeLater {
        if (SystemTray.isSupported()) {
            try {
                val trayIconImage = loadSVG("drawable/ic_launcher_yin_yang.svg", White, 24)
                val trayIcon = TrayIcon(trayIconImage, "Cuer").apply {
                    isImageAutoSize = true
                    this.toolTip = "Cuer"
                    // this.popupMenu = createPopup()
                }

                trayIcon.addMouseListener(object : MouseListener {
                    override fun mouseClicked(e: MouseEvent?) {
                        if (popup.isVisible) {
                            popup.hidePopup()
                        } else {
                            popup.showPopup()
                        }
                    }

                    override fun mousePressed(e: MouseEvent?) = Unit
                    override fun mouseReleased(e: MouseEvent?) = Unit
                    override fun mouseEntered(e: MouseEvent?) = Unit
                    override fun mouseExited(e: MouseEvent?) = Unit

                })
                val systemTray = SystemTray.getSystemTray()
                systemTray.add(trayIcon)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPopup(): PopupMenu {
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
        return popup
    }
}
