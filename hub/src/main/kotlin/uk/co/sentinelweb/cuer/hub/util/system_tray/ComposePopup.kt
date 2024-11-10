package uk.co.sentinelweb.cuer.hub.util.system_tray

import androidx.compose.ui.awt.ComposePanel
import uk.co.sentinelweb.cuer.hub.util.system_tray.SystemTrayPopup.CustomPopupContent
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.JFrame

class ComposePopup : JFrame() {

    init {
        defaultCloseOperation = HIDE_ON_CLOSE
        isAlwaysOnTop = true
        setUndecorated(true)
        size = Dimension(260, 50)
        layout = BorderLayout()

        // Create Compose Panel
        val composePanel = ComposePanel()

        // Set Compose content
        composePanel.setContent {
            CustomPopupContent {
                // Close the popup when the close button is clicked
                hidePopup()
            }
        }

        add(composePanel, BorderLayout.CENTER)
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
        setLocation(screenSize.width - width - 20, 20)
    }
}
