package uk.co.sentinelweb.cuer.hub.ui.player.vlc

import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import java.awt.event.ActionEvent
import javax.swing.*

class VlcPlayerKeyMap {
    private lateinit var mediaPlayerComponent: CallbackMediaPlayerComponent
    private lateinit var frame: JFrame
    private lateinit var coordinator: VlcPlayerUiCoordinator

    // the Action for when Left key is pressed
    val skipBackAction: Action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            println("Skip back")
            coordinator.dispatch(SkipBackClicked)
        }
    }


    // the Action for when Right key is pressed
    val skipForwardAction: Action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            println("Skip forward")
            coordinator.dispatch(SkipFwdClicked)
        }
    }


    // the Action for when Space key is pressed
    val playPauseAction: Action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            println("Play/Pause")
            coordinator.dispatch(PlayPauseClicked())
        }
    }

    val volUpAction: Action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            println("Vol up")
            val currentVolume = mediaPlayerComponent.mediaPlayer().audio().volume()
            val newVolume = (currentVolume + 4).coerceIn(0, 200)
            coordinator.dispatch(VolumeChanged(newVolume.toFloat()))
        }
    }

    val volDownAction: Action = object : AbstractAction() {
        override fun actionPerformed(e: ActionEvent) {
            println("Vol up")
            val currentVolume = mediaPlayerComponent.mediaPlayer().audio().volume()
            val newVolume = (currentVolume - 4).coerceIn(0, 200)
            coordinator.dispatch(VolumeChanged(newVolume.toFloat()))
        }
    }

    fun initialiseKeyMap(
        mediaPlayerComponent: CallbackMediaPlayerComponent,
        frame: JFrame,
        coordinator: VlcPlayerUiCoordinator
    ) {
        this.frame = frame
        this.mediaPlayerComponent = mediaPlayerComponent
        this.coordinator = coordinator

        // associate the Actions with the Left, Right, and Space keys

        attachKeymap(
            component = mediaPlayerComponent.videoSurfaceComponent(),
        )
    }

    private fun attachKeymap(
        component: JComponent,
    ) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("LEFT"), "skipBack")
        component.getActionMap().put("skipBack", skipBackAction)

        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("RIGHT"), "skipForward")
        component.getActionMap().put("skipForward", skipForwardAction)

        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("SPACE"), "playPause")
        component.getActionMap().put("playPause", playPauseAction)

        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("UP"), "volumeUp")
        component.getActionMap().put("volumeUp", volUpAction)

        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("DOWN"), "volumeDown")
        component.getActionMap().put("volumeDown", volDownAction)

    }

}