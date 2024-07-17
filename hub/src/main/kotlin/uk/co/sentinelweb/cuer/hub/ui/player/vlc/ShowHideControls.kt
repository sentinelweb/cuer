package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import java.awt.Cursor
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.Timer

class ShowHideControls {
    private lateinit var controlsPane: JPanel
    private lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent
    private lateinit var frame: JFrame
    private val transparentCursor: Cursor

    init {
        val toolkit = Toolkit.getDefaultToolkit()
        val cursorImage = toolkit.createImage(ByteArray(0))
        transparentCursor = toolkit.createCustomCursor(cursorImage, Point(0, 0), "invisibleCursor")
    }

    private val hideControls = {
        controlsPane.isVisible = false
        this.frame.cursor = transparentCursor
    }

    private val hideControlsTimer = Timer(3000, { hideControls() })
        .apply { isRepeats = false }

    fun setupInactivityTimer(
        frame: JFrame,
        mediaPlayerComponent: CallbackMediaPlayerComponent,
        controlsPane: JPanel
    ) {
        this.controlsPane = controlsPane
        this.frame = frame
        this.mediaPlayerComponent = mediaPlayerComponent

        // Mouse adapter to handle mouse events
        val activityListener = object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent?) {
                showControls()
            }

            override fun mouseClicked(e: MouseEvent?) {
                showControls()
            }

            override fun mouseEntered(e: MouseEvent?) {
                showControls()
            }

            override fun mouseExited(e: MouseEvent?) {
                hideControlsTimer.restart()
            }
        }
        mediaPlayerComponent.videoSurfaceComponent().addMouseMotionListener(activityListener)
        mediaPlayerComponent.videoSurfaceComponent().addMouseListener(activityListener)

        hideControlsTimer.restart()
    }

    fun showControls() {
        if (!controlsPane.isVisible) {
            controlsPane.isVisible = true
            this.frame.cursor = Cursor.getDefaultCursor()
            hideControlsTimer.restart()
        }
    }
}