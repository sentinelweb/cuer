package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import androidx.compose.ui.graphics.Color.Companion.Black
import loadSVG
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import toImageIcon
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
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import java.awt.BorderLayout
import java.awt.BorderLayout.*
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.ChangeEvent


class VlcPlayerSwingWindow(
    private val coordinator: VlcPlayerUiCoordinator,
) : JFrame(), KoinComponent {


    lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent
    private val log: LogWrapper by inject()
    private val timeProvider: TimeProvider by inject()

    private lateinit var playButton: JButton
    private lateinit var pauseButton: JButton
    private lateinit var rewindButton: JButton
    private lateinit var forwardButton: JButton
    private lateinit var prevButton: JButton
    private lateinit var nextButton: JButton
    private lateinit var seekBar: JSlider
    private lateinit var posText: JLabel
    private lateinit var durText: JLabel

    private var durationMs: Long? = null

    val seekChangeListner: (e: ChangeEvent) -> Unit = { e ->
        val source = e.source as JSlider
        if (!source.valueIsAdjusting) {
            val posValue = source.value.toFloat() // between 0 and 1000
            coordinator.dispatch(SeekBarChanged(posValue / source.maximum))
        }
    }
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
        this.contentPane.add(mediaPlayerComponent, CENTER)
        mediaPlayerComponent.mediaPlayer().events().addMediaEventListener(
            object : MediaEventAdapter() {
                override fun mediaParsedChanged(media: Media, newStatus: MediaParsedStatus) {
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
                    // also send PLAYING on ready
                    coordinator.dispatch(PlayerStateChanged(PLAYING))
                }

                override fun paused(mediaPlayer: MediaPlayer?) {
                    log.d("event paused")
                    coordinator.dispatch(PlayerStateChanged(PAUSED))
                }

                override fun finished(mediaPlayer: MediaPlayer?) {
                    log.d("event finished")
                    coordinator.dispatch(PlayerStateChanged(ENDED))
                }

                var lastPosUpdateTime = 0L
                override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                    val current = timeProvider.currentTimeMillis()
                    if (current - lastPosUpdateTime > 200) {
                        durationMs?.let {
                            val newPositionLong = (newPosition * it).toLong()
                            coordinator.dispatch(PositionReceived(newPositionLong))
                        }
                        lastPosUpdateTime = current
                    }
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
        controlsPane.layout = BorderLayout()
        controlsPane.background = Color.BLACK

        val buttonsPane = JPanel()
        buttonsPane.background = Color.BLACK
        controlsPane.add(buttonsPane, CENTER)

        val seekPane = JPanel()
        seekPane.layout = BorderLayout()
        seekPane.background = Color.BLACK
        controlsPane.add(seekPane, SOUTH)

        this.contentPane.add(controlsPane, SOUTH)

        prevButton = JButton("Previous").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_track_b.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(TrackBackClicked) }
        }
        rewindButton = JButton("Rewind").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_fast_rewind.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(SkipBackClicked) }
        }
        playButton = JButton("Play").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_play.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(PlayPauseClicked(false)) }
        }
        pauseButton = JButton("Pause").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_pause.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(PlayPauseClicked(true)) }
        }
        forwardButton = JButton("Skip").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_fast_forward.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(SkipFwdClicked) }
        }
        nextButton = JButton("Next").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_player_track_f.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.dispatch(TrackFwdClicked) }
        }
        seekBar = JSlider(0, 1000, 0).apply {
            seekPane.add(this, CENTER)

            addChangeListener(seekChangeListner)
        }
        posText = JLabel("00:00:00").apply {
            seekPane.add(this, WEST)
            foreground = Color.WHITE

        }
        durText = JLabel("00:00:00").apply {
            seekPane.add(this, EAST)
            foreground = Color.WHITE
        }
    }

    fun updateUiPlayState(state: PlayerStateDomain) {
        log.d("state:${state::class.java.simpleName}")
        return when (state) {
            UNKNOWN -> Unit
            UNSTARTED -> Unit
            ENDED -> Unit
            PLAYING -> {
                pauseButton.isVisible = true
                playButton.isVisible = false
            }

            PAUSED -> {
                pauseButton.isVisible = false
                playButton.isVisible = true
            }

            BUFFERING -> Unit
            VIDEO_CUED -> Unit
            PlayerStateDomain.ERROR -> Unit
        }
    }

    fun updateUiTimes(times: PlayerContract.View.Model.Times) {
        seekBar.removeChangeListener(seekChangeListner)
        seekBar.value = (times.seekBarFraction * seekBar.maximum).toInt()
        seekBar.addChangeListener(seekChangeListner)
        posText.text = times.positionText
        durText.text = times.durationText
    }

    fun playStateChanged(command: PlayerContract.PlayerCommand) = when (command) {
        is Load -> playItem(command.platformId)
        is Pause -> mediaPlayerComponent.mediaPlayer().controls().pause()
        is Play -> mediaPlayerComponent.mediaPlayer().controls().play()
        is SkipFwd -> mediaPlayerComponent.mediaPlayer().controls().skipTime(command.ms.toLong())
        is SkipBack -> mediaPlayerComponent.mediaPlayer().controls().skipTime(-command.ms.toLong())
        is SeekTo -> mediaPlayerComponent.mediaPlayer().controls().setTime(command.ms)
    }.also { log.d("command:${command::class.java.simpleName}") }

    fun updateTexts(texts: PlayerContract.View.Model.Texts) {
        title = texts.title
        forwardButton.text = "Forward [${texts.skipFwdText}]"
        rewindButton.text = "Rewind [${texts.skipBackText}]"
    }

    fun updateButtons(it: PlayerContract.View.Model.Buttons) {
        nextButton.isEnabled = it.nextTrackEnabled
        prevButton.isEnabled = it.prevTrackEnabled
        seekBar.isEnabled = it.seekEnabled
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