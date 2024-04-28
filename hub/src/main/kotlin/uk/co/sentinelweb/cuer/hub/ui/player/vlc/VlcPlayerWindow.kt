package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities


class VlcPlayerWindow {

    lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent

    fun showWindow(file: MediaDomain) {
        SwingUtilities.invokeLater {
            // Discover and set libVLC directory
            if (!NativeDiscovery().discover()) {
                println("Could not find VLC installation, please set VLC_PLUGIN_PATH environment variable")
                System.exit(1)
            }

            mediaPlayerComponent = CallbackMediaPlayerComponent()

            val frame = JFrame("VLC in Swing Application")
            frame.defaultCloseOperation = JFrame.DO_NOTHING_ON_CLOSE

            frame.layout = BorderLayout()

            frame.contentPane.add(mediaPlayerComponent, BorderLayout.CENTER)

            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val screenDevices = ge.screenDevices
            val preferredScreen = 1
            val selectedScreen = if (screenDevices.size > preferredScreen + 1) {
                preferredScreen
            } else 0
            val secondScreen = screenDevices[selectedScreen]
            val config = secondScreen.defaultConfiguration
            val bounds = config.bounds
            frame.setLocation(bounds.x, bounds.y)
            frame.setSize(bounds.width - 100, bounds.height - 200)


            frame.isVisible = true

            // Handle window closing operation
            frame.addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    mediaPlayerComponent.mediaPlayer().release()
                    frame.dispose()
                }
            })

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
                mediaPlayerComponent.mediaPlayer().controls().pause();
            }

            rewindButton.addActionListener {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(-10000);
            }

            skipButton.addActionListener {
                mediaPlayerComponent.mediaPlayer().controls().skipTime(10000);
            }
            skipButton.addActionListener {
                mediaPlayerComponent.mediaPlayer().fullScreen();
            }
            frame.contentPane.add(controlsPane, BorderLayout.SOUTH)

            mediaPlayerComponent.mediaPlayer().media().play(file.platformId)
        }
    }

}
//            val videoSurfacePanel = JPanel()
//            videoSurfacePanel.background = Color.red
//            videoSurfacePanel.layout = BorderLayout()
//            videoSurfacePanel.add(mediaPlayerComponent.videoSurfaceComponent(), BorderLayout.CENTER)
//            videoSurfacePanel.isVisible = true
//            frame.contentPane.add(videoSurfacePanel, BorderLayout.CENTER)
