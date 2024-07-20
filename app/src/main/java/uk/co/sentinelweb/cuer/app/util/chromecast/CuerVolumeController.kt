package uk.co.sentinelweb.cuer.app.util.chromecast

import android.view.KeyEvent
import uk.co.sentinelweb.cuer.app.ui.common.views.CastVolumeControlView

/**
 * This isn't great but it (just) works
 * - doesn't show the volume sliders - might need to use activity.volumeControlStream - need to find cast stream id
 * - there is probably a better way to do it with cast API or a MediaButtonReceiver
 */
class CuerSimpleVolumeController constructor(
    private val chromeCastSessionListener: ChromeCastSessionListener
) {
    var controlView: CastVolumeControlView? = null

    fun handleVolumeKey(event: KeyEvent): Boolean {
        val action: Int = event.getAction()
        val keyCode: Int = event.getKeyCode()
        return chromeCastSessionListener.currentCastSession?.let { castSession ->
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        castSession.volume = castSession.volume + VOL_INCREMENT
                        controlView?.updateValue()
                    }
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (action == KeyEvent.ACTION_DOWN) {
                        castSession.volume = castSession.volume - VOL_INCREMENT
                        controlView?.updateValue()
                    }
                    true
                }
                else -> false
            }
        } ?: false
    }

    companion object {
        private val VOL_INCREMENT = 0.05
    }
}