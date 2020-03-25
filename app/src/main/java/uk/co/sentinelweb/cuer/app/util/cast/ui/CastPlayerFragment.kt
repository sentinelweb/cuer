package uk.co.sentinelweb.cuer.app.util.cast.ui

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.cast_player_view.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class CastPlayerFragment() : Fragment(R.layout.cast_player_view), CastPlayerContract.View {

    private val presenter: CastPlayerContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()

    override val presenterExternal: CastPlayerContract.PresenterExternal
        get() = presenter as CastPlayerContract.PresenterExternal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cast_player_play.setOnClickListener { presenter.onPlayPressed() }
        cast_player_pause.setOnClickListener { presenter.onPausePressed() }
        cast_player_seek_back.setOnClickListener { presenter.onSeekBackPressed() }
        cast_player_seek_forward.setOnClickListener { presenter.onSeekFwdPressed() }
        cast_player_track_last.setOnClickListener { presenter.onTrackBackPressed() }
        cast_player_track_next.setOnClickListener { presenter.onTrackFwdPressed() }
        cast_player_seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    presenter.onSeekChanged(progress.toFloat() / seekBar.max)
                }
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(view: SeekBar) {
                presenter.onSeekFinished()
            }
        })
    }

    override fun initMediaRouteButton() {
        chromeCastWrapper.initMediaRouteButton(media_route_button)
    }

    override fun setConnectionText(text: String) {
        cast_player_status_text.text = text
    }

    override fun setCurrentSecond(second: String) {
        cast_player_current_time.text = second
    }

    override fun setDuration(duration: String) {
        cast_player_duration.text = duration
    }

    override fun setPlaying() {
        cast_player_play.isVisible = false
        cast_player_pause.isVisible = true
        cast_player_buffering.isVisible = false
    }

    override fun setPaused() {
        cast_player_play.isVisible = true
        cast_player_pause.isVisible = false
        cast_player_buffering.isVisible = false
    }

    override fun setBuffering() {
        cast_player_play.isVisible = false
        cast_player_pause.isVisible = false
        cast_player_buffering.isVisible = true
    }

    override fun showMessage(msg: String) {
        Snackbar.make(view!!,msg,Snackbar.LENGTH_LONG).show()
    }

    override fun setTitle(title: String) {
        cast_player_title.text = title
    }

    override fun updateSeekPosition(ratio: Float) {
        cast_player_seek.progress = (ratio * cast_player_seek.max).toInt()
    }

    companion object {
        @JvmStatic
        val viewModule = module {
            scope(named<CastPlayerFragment>()) {
                scoped<CastPlayerContract.View> { getSource() }
                scoped<CastPlayerContract.Presenter> { CastPlayerPresenter(get(), get()) }
                viewModel { CastPlayerState() }
            }
        }
    }
}