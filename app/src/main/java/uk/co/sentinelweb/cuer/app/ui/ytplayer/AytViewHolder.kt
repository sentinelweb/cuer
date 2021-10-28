package uk.co.sentinelweb.cuer.app.ui.ytplayer

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import uk.co.sentinelweb.cuer.app.R

class AytViewHolder {
    private var _view: YouTubePlayerView? = null

    val isHolding: Boolean
        get() = _view != null

    val view: YouTubePlayerView?
        get() = _view

//    fun hold(view: YouTubePlayerView) {
//        _view = view
//    }

    fun release() {
        _view = null
    }

    fun create(activity: AppCompatActivity, parent: FrameLayout) {
        _view = LayoutInflater.from(activity).inflate(R.layout.view_ayt_video, parent) as YouTubePlayerView
        parent.addView(_view, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    fun remove(parent: FrameLayout) {
        parent.removeView(_view)
    }


}