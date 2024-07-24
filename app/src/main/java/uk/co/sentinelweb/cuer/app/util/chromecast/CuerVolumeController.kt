package uk.co.sentinelweb.cuer.app.util.chromecast

import android.view.KeyEvent
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.common.views.CastVolumeControlView
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

/**
 * This isn't great but it (just) works
 * - doesn't show the volume sliders - might need to use activity.volumeControlStream - need to find cast stream id
 * - there is probably a better way to do it with cast API or a MediaButtonReceiver
 */
class CuerSimpleVolumeController(
    private val castController: CastController,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    var controlView: CastVolumeControlView? = null
        get() = field
        set(value) {
            value?.castController = castController
            field = value
        }

    fun handleVolumeKey(event: KeyEvent): Boolean {
        val action: Int = event.getAction()
        val keyCode: Int = event.getKeyCode()

        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    val newVolume = castController.getVolume() + VOL_INCREMENT
                    log.d("newVolume; $newVolume")
                    castController.setVolume(newVolume)
                    controlView?.updateValue()
                }
                true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    val newVolume = castController.getVolume() - VOL_INCREMENT
                    castController.setVolume(newVolume)
                    log.d("newVolume; $newVolume")
                    controlView?.updateValue()
                }
                true
            }

            else -> false
        }
    }

    companion object {
        private val VOL_INCREMENT = 0.03f
    }
}
