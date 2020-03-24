package uk.co.sentinelweb.cuer.app.util.cast.ui

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.cast_player_view.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class CastPlayerFragment() : Fragment(R.layout.cast_player_view), CastPlayerContract.View {

    private val presenter: CastPlayerContract.Presenter by currentScope.inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cast_player_play.setOnClickListener { presenter.playPressed() }
        cast_player_pause.setOnClickListener { presenter.pausePressed() }
        cast_player_seek_back.setOnClickListener { presenter.seekBackPressed() }
        cast_player_seek_forward.setOnClickListener { presenter.seekFwdPressed() }
        cast_player_track_last.setOnClickListener { presenter.trackBackPressed() }
        cast_player_track_next.setOnClickListener { presenter.trackFwdPressed() }
        cast_player_seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    presenter.onSeekChanged(progress.toFloat()/seekBar.max)
                }
            }
            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(view: SeekBar) {}
        })
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