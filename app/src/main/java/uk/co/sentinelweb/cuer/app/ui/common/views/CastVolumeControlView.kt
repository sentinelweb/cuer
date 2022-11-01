package uk.co.sentinelweb.cuer.app.ui.common.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ViewCastVolumeBinding
import uk.co.sentinelweb.cuer.app.util.cast.CuerCastSessionListener
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeIn
import uk.co.sentinelweb.cuer.app.util.extension.view.fadeOut
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

class CastVolumeControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.seekBarStyle
) : LinearLayout(context, attrs, defStyle), KoinComponent {

    private val res: ResourceWrapper by inject()
    private val cuerCastSessionListener: CuerCastSessionListener by inject()
    private val contextProvider: CoroutineContextProvider by inject()
    private val timeProvider: TimeProvider by inject()

    private var _binding: ViewCastVolumeBinding? = null
    private val binding get() = _binding!!

    private var hideJob: Job? = null

    private var lastUpdateLong = 0L

    @DrawableRes
    private var lastVolumeDrawable = R.drawable.ic_volume_up_24

    private val level get() = binding.cvVolume.progress.toDouble() / binding.cvVolume.max

    init {
        _binding = ViewCastVolumeBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.root.setOnClickListener { }
        binding.cvVolumeMuteIcon.setOnClickListener {
            cuerCastSessionListener.currentCastSession
                ?.apply { volume = 0.0 }
                ?.apply { binding.cvVolume.progress = 0 }
                ?.apply { updateIcon() }
        }
        binding.cvVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (timeProvider.currentTimeMillis() - lastUpdateLong > 300) {
                        cuerCastSessionListener.currentCastSession
                            ?.apply { volume = progress.toDouble() / binding.cvVolume.max }
                        lastUpdateLong = timeProvider.currentTimeMillis()
                        triggerHide()
                        updateIcon()
                    }
                }
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(view: SeekBar) {
                cuerCastSessionListener.currentCastSession
                    ?.apply { volume = level }
                    ?.apply { updateIcon() }
            }
        })
    }

    fun updateIcon() {
        if (level >= 0.7) {
            setIconIfNecessary(R.drawable.ic_volume_up_24)
        } else if (level >= 0.2) {
            setIconIfNecessary(R.drawable.ic_volume_down_24)
        } else {
            setIconIfNecessary(R.drawable.ic_volume_mute_24)
        }
    }

    private fun setIconIfNecessary(@DrawableRes id: Int) {
        if (lastVolumeDrawable != id) {
            lastVolumeDrawable = id
            binding.cvVolumeIcon.setImageDrawable(res.getDrawable(id))
        }
    }

    fun show() {
        if (!isVisible) {
            fadeIn()
        }
        triggerHide()
    }

    private fun triggerHide() {
        hideJob?.cancel()
        hideJob = contextProvider.mainScope.launch {
            delay(3000)
            hide()
        }
    }

    fun hide() {
        fadeOut()
    }

    fun updateValue() {
        show()
        cuerCastSessionListener.currentCastSession
            ?.apply { binding.cvVolume.setProgress((volume * binding.cvVolume.max).toInt()) }
        updateIcon()
    }
}
