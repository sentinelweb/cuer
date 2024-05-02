package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel


class VlcPlayerSwingWindow : JFrame() {

    lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent

    init {
        createWindow()
    }

    private fun createWindow() {

        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE

        this.layout = BorderLayout()


        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevices = ge.screenDevices
        val preferredScreen = 1
        val selectedScreen = if (screenDevices.size > preferredScreen + 1) {
            preferredScreen
        } else 0
        val secondScreen = screenDevices[selectedScreen]
        val config = secondScreen.defaultConfiguration
        val bounds = config.bounds
        this.setLocation(bounds.x, bounds.y)
        this.setSize(bounds.width - 100, bounds.height - 200)

        this.isVisible = true
    }

    fun createUi(coordinator: VlcPlayerUiCoordinator) {
        mediaPlayerComponent = CallbackMediaPlayerComponent()
        this.contentPane.add(mediaPlayerComponent, BorderLayout.CENTER)

        // Handle window closing operation
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                coordinator.playerWindowDestroyed()
            }
        })
    }

    fun playItem(path: String) {
        mediaPlayerComponent.mediaPlayer().media().play(path)
    }

    fun destroy() {
        mediaPlayerComponent.mediaPlayer().release()
        this@VlcPlayerSwingWindow.dispose()
    }

    private fun createControls() {
        val controlsPane = JPanel()

        val pauseButton = JButton("Pause")
        controlsPane.add(pauseButton)

        val rewindButton = JButton("<< Rewind")
        controlsPane.add(rewindButton)

        val skipButton = JButton("Skip >>")
        controlsPane.add(skipButton)

        val fullButton = JButton("Fullscreen")
        controlsPane.add(fullButton)

        pauseButton.addActionListener {
            mediaPlayerComponent.mediaPlayer().controls().pause()
        }

        rewindButton.addActionListener {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000)
        }

        skipButton.addActionListener {
            mediaPlayerComponent.mediaPlayer().controls().skipTime(10000)
        }
        skipButton.addActionListener {
            mediaPlayerComponent.mediaPlayer().fullScreen()
        }
        this.contentPane.add(controlsPane, BorderLayout.SOUTH)
    }

    companion object {
        fun showWindow(coordinator: VlcPlayerUiCoordinator): VlcPlayerSwingWindow? {
            //SwingUtilities.invokeLater {
            // Discover and set libVLC directory
            if (!NativeDiscovery().discover()) {
                val message = "Could not find VLC installation, please set VLC_PLUGIN_PATH environment variable"
                println(message)
                //System.exit(1)
                JOptionPane.showMessageDialog(
                    null,  // parent component, can be null if not considering location of dialog
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return null
            } else {
                val frame = VlcPlayerSwingWindow()
                frame.createWindow()
                frame.createUi(coordinator)
                frame.createControls()
                return frame
            }
            //}
        }
    }

}
//            val videoSurfacePanel = JPanel()
//            videoSurfacePanel.background = Color.red
//            videoSurfacePanel.layout = BorderLayout()
//            videoSurfacePanel.add(mediaPlayerComponent.videoSurfaceComponent(), BorderLayout.CENTER)
//            videoSurfacePanel.isVisible = true
//            frame.contentPane.add(videoSurfacePanel, BorderLayout.CENTER)
