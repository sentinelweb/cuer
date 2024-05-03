package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.media.Media
import uk.co.caprica.vlcj.media.MediaEventAdapter
import uk.co.caprica.vlcj.media.MediaParsedStatus
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import java.awt.BorderLayout
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel


class VlcPlayerSwingWindow(
    private val coordinator: VlcPlayerUiCoordinator
) : JFrame(), KoinComponent {


    lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent
    private val log: LogWrapper by inject()

    private lateinit var playButton: JButton
    private lateinit var pauseButton: JButton
    private lateinit var rewindButton: JButton
    private lateinit var skipButton: JButton

    private var durationMs: Long? = null

    init {
        log.tag(this)
        createWindow()
    }

    private fun createWindow() {
        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE

        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevices = ge.screenDevices
        log.d("screens: ${screenDevices.size}")
        val preferredScreen = 2//1
        val preferredExists = screenDevices.size > preferredScreen
        val selectedScreenIndex = if (preferredExists) {
            preferredScreen
        } else 0
        log.d("selectedScreenIndex: $selectedScreenIndex")
        val selectedScreen = screenDevices[selectedScreenIndex]
        val config = selectedScreen.defaultConfiguration
        val bounds = config.bounds
        this.setLocation(bounds.x, bounds.y)
        if (preferredExists) {
            this.setSize(bounds.width, bounds.height)
        } else {
            this.setSize(640, 480)
        }
        // fixme throwing an error but the frame should not be visible yet
        //this.isUndecorated = preferredExists
        this.layout = BorderLayout()
        // Handle window closing operation
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                coordinator.playerWindowDestroyed()
            }
        })
        this.isVisible = true
    }

    fun createMediaPlayer() {
        mediaPlayerComponent = CallbackMediaPlayerComponent()
        this.contentPane.add(mediaPlayerComponent, BorderLayout.CENTER)
        mediaPlayerComponent.mediaPlayer().events().addMediaEventListener(
            object : MediaEventAdapter() {
                override fun mediaParsedChanged(media: Media, newStatus: MediaParsedStatus) {
                    super.mediaParsedChanged(media, newStatus)
                    val ms = media.info().duration()
                    log.d("duration: $ms")
                    durationMs = ms
                    coordinator.dispatch(DurationReceived(ms))
                    mediaPlayerComponent.mediaPlayer().media().play(media.newMediaRef())
                }
            }
        )

        mediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(
            object : MediaPlayerEventAdapter() {

                override fun mediaChanged(mediaPlayer: MediaPlayer?, media: MediaRef?) {
                    log.d("event mediaChanged: ${media}")
                }

                override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
                    log.d("event buffering")
                    coordinator.dispatch(PlayerStateChanged(BUFFERING))
                }

                override fun playing(mediaPlayer: MediaPlayer?) {
                    log.d("event playing")
                    // send on ready
                    //coordinator.dispatch(PlayerStateChanged(PLAYING))
                }

                override fun paused(mediaPlayer: MediaPlayer?) {
                    log.d("event paused")
                    coordinator.dispatch(PlayerStateChanged(PAUSED))
                }

                override fun finished(mediaPlayer: MediaPlayer?) {
                    log.d("event finished")
                    coordinator.dispatch(PlayerStateChanged(ENDED))
                }

                override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                    //log.d("event positionChanged: $newPosition")
                    durationMs?.let { coordinator.dispatch(PositionReceived((newPosition * it).toLong())) }
                }

                override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
                    log.d("event ready")
                    coordinator.dispatch(PlayerStateChanged(PLAYING))
                }

                override fun error(mediaPlayer: MediaPlayer?) {
                    coordinator.dispatch(PlayerStateChanged(PlayerStateDomain.ERROR))
                }
            }
        )
    }

    fun playItem(path: String) {
        durationMs = null
        mediaPlayerComponent.mediaPlayer().media().prepare(path)
        mediaPlayerComponent.mediaPlayer().media().parsing().parse()
        // play after parse is complete
//        mediaPlayerComponent.mediaPlayer().media().play(path)
    }

    fun destroy() {
        mediaPlayerComponent.mediaPlayer().release()
        this@VlcPlayerSwingWindow.dispose()
    }

    private fun createControls() {
        val controlsPane = JPanel()

        playButton = JButton("Play").apply {
            controlsPane.add(this)
            addActionListener { coordinator.dispatch(PlayPauseClicked(false)) }
        }
        pauseButton = JButton("Pause").apply {
            controlsPane.add(this)
            addActionListener { coordinator.dispatch(PlayPauseClicked(true)) }
        }
        rewindButton = JButton("<< Rewind").apply {
            controlsPane.add(this)
            addActionListener { coordinator.dispatch(SkipBackClicked) }
        }
        skipButton = JButton("Skip >>").apply {
            controlsPane.add(this)
            addActionListener { coordinator.dispatch(SkipFwdClicked) }
        }

        this.contentPane.add(controlsPane, BorderLayout.SOUTH)
    }

    fun updateUiPlayState(state: PlayerStateDomain) = when (state) {
        UNKNOWN -> Unit
        UNSTARTED -> Unit
        ENDED -> Unit
        PLAYING -> {
//            pauseButton.isVisible = true
//            playButton.isVisible = false
        }

        PAUSED -> {
//            pauseButton.isVisible = false
//            playButton.isVisible = true
        }

        BUFFERING -> Unit
        VIDEO_CUED -> Unit
        PlayerStateDomain.ERROR -> Unit
    }

    fun playState(command: PlayerContract.PlayerCommand) = when (command) {
        is Load -> playItem(command.platformId)
        is Pause -> mediaPlayerComponent.mediaPlayer().controls().pause()
        is Play -> mediaPlayerComponent.mediaPlayer().controls().play()
        is SkipFwd -> mediaPlayerComponent.mediaPlayer().controls().skipTime(command.ms.toLong())
        is SkipBack -> mediaPlayerComponent.mediaPlayer().controls().skipTime(-command.ms.toLong())
        is SeekTo -> mediaPlayerComponent.mediaPlayer().controls().setTime(command.ms)
    }

    companion object {
        fun showWindow(coordinator: VlcPlayerUiCoordinator): VlcPlayerSwingWindow? {
            if (!NativeDiscovery().discover()) {
                val message = "Could not find VLC installation, please set VLC_PLUGIN_PATH environment variable"
                println(message)
                JOptionPane.showMessageDialog(
                    null,  // parent component, can be null if not considering location of dialog
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
                return null
            } else {
                val frame = VlcPlayerSwingWindow(coordinator)
                frame.createWindow()
                frame.createMediaPlayer()
                frame.createControls()
                return frame
            }
        }
    }
}

//        val fullButton = JButton("Fullscreen")
//        controlsPane.add(fullButton)
//        skipButton.addActionListener {
//            mediaPlayerComponent.mediaPlayer().fullScreen()
//        }