package uk.co.sentinelweb.cuer.app.ui.common.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.isVisible
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class PlayYangProgress(private val res: ResourceWrapper) {

    private var _progressBar: ProgressBar? = null
    private var _progressDrawable: Drawable? = null
    private var _progressAnim: ObjectAnimator? = null

    fun init(progress: ProgressBar) {
        _progressBar = progress
        _progressBar?.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                if (_progressBar?.isVisible ?: false && _progressAnim == null) {
                    showLoading()
                } else if (_progressBar?.isVisible ?: false && _progressAnim != null) {
                    hideLoading()
                }
            }
        })
        _progressDrawable = res.getDrawable(R.drawable.ic_play_yang_loading)
        _progressDrawable?.setTint(res.getColor(R.color.build_primary))
        _progressBar?.indeterminateDrawable = _progressDrawable
    }

    fun showLoading() {
        _progressBar?.isVisible = true
        _progressAnim = ObjectAnimator.ofFloat(_progressBar, "rotation", 0f, 360f)
            .apply {
                setDuration(1000);
                setRepeatCount(ValueAnimator.INFINITE);
                start()
            }
    }

    fun hideLoading() {
        _progressAnim?.cancel()
        _progressAnim = null
        _progressBar?.isVisible = false
    }


}