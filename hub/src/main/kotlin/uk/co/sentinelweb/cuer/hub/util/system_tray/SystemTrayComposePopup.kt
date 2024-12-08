package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.ui.awt.ComposePanel
import uk.co.sentinelweb.cuer.hub.util.system_tray.ComposeSystemTrayPopup.SystemTrayComposePopup
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.JFrame
import javax.swing.JPanel

class SystemTrayComposePopup : JFrame() {

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        isAlwaysOnTop = true
        setUndecorated(true)
        size = Dimension(580, 100)
        layout = BorderLayout()

        // Create Compose Panel
        val composePanel = ComposePanel()

        // Set Compose content
        composePanel.setContent {
            SystemTrayComposePopup()
        }

        // Create JPanel and add ComposePanel
        val panel = JPanel(BorderLayout()).apply {
            add(composePanel, BorderLayout.CENTER)
        }

        // Add JPanel to JFrame
        add(panel, BorderLayout.CENTER)

        positionAtTopRight()
        // Hide initially
        isVisible = false
    }

    fun showPopup() {
        isVisible = true
    }

    fun hidePopup() {
        isVisible = false
    }

    private fun positionAtTopRight() {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        setLocation(screenSize.width - width - 20, 5)
    }
}
