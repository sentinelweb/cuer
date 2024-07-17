package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import androidx.compose.ui.graphics.Color.Companion.Black
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain.*
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator.Companion.PREFERRED_SCREEN_DEFAULT
import java.awt.BorderLayout
import java.awt.BorderLayout.*
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.event.MouseWheelEvent
import java.awt.event.MouseWheelListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.JOptionPane.ERROR_MESSAGE
import javax.swing.event.ChangeEvent


@ExperimentalCoroutinesApi
class VlcPlayerSwingWindow(
    private val coordinator: VlcPlayerUiCoordinator,
    private val folderListUseCase: GetFolderListUseCase,
    private val showHideControls: ShowHideControls,
) : JFrame(), KoinComponent {


    lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent
    private val log: LogWrapper by inject()
    private val timeProvider: TimeProvider by inject()

    private lateinit var playButton: JButton
    private lateinit var pauseButton: JButton
    private lateinit var stopButton: JButton
    private lateinit var rewindButton: JButton
    private lateinit var forwardButton: JButton
    private lateinit var prevButton: JButton
    private lateinit var nextButton: JButton
    private lateinit var seekBar: PromrammaticallyChangingSlider
    private lateinit var posText: JLabel
    private lateinit var durText: JLabel
    private lateinit var titleText: JLabel
    private lateinit var bufferText: JLabel
    private lateinit var volumeText: JLabel
    private lateinit var controlsPane: JPanel

    private var durationMs: Long? = null

    val seekChangeListner: (e: ChangeEvent) -> Unit = { e ->
        val source = e.source as JSlider
        if (!source.valueIsAdjusting) {
            val posValue = source.value.toFloat() // between 0 and 1000
            val fraction = posValue / source.maximum
            coordinator.dispatch(SeekBarChanged(fraction))
        }
    }

    init {
        log.tag(this)
    }

    fun assemble(screenIndex: Int = PREFERRED_SCREEN_DEFAULT) {
        createWindow(screenIndex)
        createMediaPlayer()
        createControls()
        showHideControls.setupInactivityTimer(this, mediaPlayerComponent, controlsPane)
    }

    private fun createWindow(preferredScreen: Int) {
        this.defaultCloseOperation = DO_NOTHING_ON_CLOSE

        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevices = ge.screenDevices
        val preferredExists = screenDevices.size > preferredScreen
        val selectedScreenIndex = if (preferredExists) {
            preferredScreen
        } else 0
        log.d("selectedScreenIndex: $selectedScreenIndex, preferredScreen: $preferredScreen")
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
        this.isUndecorated = preferredExists
        this.layout = BorderLayout()
        // Handle window closing operation
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                coordinator.playerWindowDestroyed()
            }
        })
        this.isVisible = true
    }

    // https://wiki.videolan.org/VLC_command-line_help/
    private fun createMediaPlayer() {
        mediaPlayerComponent =
            CallbackMediaPlayerComponent(
                "--stereo-mode", "1",
                "--disable-screensaver",
                "--video-title-show",
                "--video-title-timeout", "3000",
            )
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
                    // log.d("event buffering")
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
                    if (current - lastPosUpdateTime > 1000) {
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
        controlsPane = JPanel()
        controlsPane.layout = BorderLayout()
        controlsPane.background = Color.BLACK

        val buttonsLayoutPane = JPanel()
        buttonsLayoutPane.background = Color.BLACK

        val buttonsPane = JPanel()
        buttonsPane.background = Color.BLACK
        controlsPane.add(buttonsLayoutPane, CENTER)
        buttonsLayoutPane.add(buttonsPane, CENTER)

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
        stopButton = JButton("Stop").apply {
            buttonsPane.add(this)
            setIcon(loadSVG("drawable/ic_stop.svg", Black, 24).toImageIcon())
            addActionListener { coordinator.playerWindowDestroyed() }
            isVisible = this@VlcPlayerSwingWindow.isUndecorated
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
        seekBar = PromrammaticallyChangingSlider(0, 1000, 0).apply {
            seekPane.add(this, CENTER)
            this.actualListener = seekChangeListner
        }
        posText = JLabel("00:00:00").apply {
            seekPane.add(this, WEST)
            foreground = Color.WHITE

        }
        durText = JLabel("00:00:00").apply {
            seekPane.add(this, EAST)
            foreground = Color.WHITE
        }
        bufferText = JLabel("[Buffering]").apply {
            buttonsLayoutPane.add(this, WEST)
            foreground = Color.WHITE
            isVisible = false
        }
        volumeText = JLabel("Volume: ").apply {
            buttonsLayoutPane.add(this, EAST)
            foreground = Color.WHITE
            isVisible = true
        }
        titleText = JLabel("[No Title]").apply {
            val textPane = JPanel()
            textPane.background = Color.BLACK
            textPane.add(this, CENTER)
            controlsPane.add(textPane, NORTH)
            foreground = Color.WHITE
            isVisible = this@VlcPlayerSwingWindow.isUndecorated
        }

        // Add a mouse wheel listener to adjust the volume
        val volumeWheelListener = object : MouseWheelListener {
            override fun mouseWheelMoved(e: MouseWheelEvent) {
                val currentVolume = mediaPlayerComponent.mediaPlayer().audio().volume()
                val newVolume = (currentVolume + e.wheelRotation * 4).coerceIn(0, 200)
                mediaPlayerComponent.mediaPlayer().audio().setVolume(newVolume)
                showHideControls.showControls()
                updateVolumeText()
            }
        }
        mediaPlayerComponent.videoSurfaceComponent().addMouseWheelListener(volumeWheelListener)
        controlsPane.addMouseWheelListener(volumeWheelListener)
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
        seekBar.value = (times.seekBarFraction * seekBar.maximum).toInt()
        posText.text = times.positionText
        durText.text = times.durationText
    }

    fun playStateChanged(command: PlayerContract.PlayerCommand) = when (command) {
        is Load -> {
            command.platformId
                .let { folderListUseCase.truncatedToFullFolderPath(it) }
                ?.also { playItem(it) }
                ?: log.d("Cannot get full path ${command.platformId}")
        }

        is Pause -> mediaPlayerComponent.mediaPlayer().controls().pause()
        is Play -> mediaPlayerComponent.mediaPlayer().controls().play()
        is SkipFwd -> mediaPlayerComponent.mediaPlayer().controls().skipTime(command.ms.toLong())
        is SkipBack -> mediaPlayerComponent.mediaPlayer().controls().skipTime(-command.ms.toLong())
        is SeekTo -> mediaPlayerComponent.mediaPlayer().controls().setTime(command.ms)
    }.also { log.d("command:${command::class.java.simpleName}") }

    fun updateTexts(texts: PlayerContract.View.Model.Texts) {
        if (!this@VlcPlayerSwingWindow.isUndecorated) {
            title = texts.title
        } else {
            titleText.text = texts.title
        }
        forwardButton.text = "Forward [${texts.skipFwdText}]"
        rewindButton.text = "Rewind [${texts.skipBackText}]"
        updateVolumeText()
    }

    fun updateVolumeText() {
        val currentVolume = mediaPlayerComponent.mediaPlayer().audio().volume()
        volumeText.text = "Volume: $currentVolume"
    }

    fun updateButtons(it: PlayerContract.View.Model.Buttons) {
        nextButton.isEnabled = it.nextTrackEnabled
        prevButton.isEnabled = it.prevTrackEnabled
        seekBar.isEnabled = it.seekEnabled
    }

    companion object {
        fun checkShowWindow(): Boolean {
            if (!NativeDiscovery().discover()) {
                // todo add installation instructions and link
                val message = "Could not find VLC installation, please set VLC_PLUGIN_PATH environment variable"
                JOptionPane.showMessageDialog(
                    null,  // parent component, can be null if not considering location of dialog
                    message,
                    "Error",
                    ERROR_MESSAGE
                )
                return false
            } else {
                return true
            }
        }
    }
}
