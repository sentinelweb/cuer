package uk.co.sentinelweb.cuer.app.util.cast.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.CastPlayerViewBinding
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class CastPlayerFragment() : Fragment(), CastPlayerContract.View {

    private val presenter: CastPlayerContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding!!

    override val presenterExternal: CastPlayerContract.PresenterExternal
        get() = presenter as CastPlayerContract.PresenterExternal

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* init */ presenter
        _binding = CastPlayerViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.castPlayerPlay.setOnClickListener { presenter.onPlayPressed() }
        binding.castPlayerPause.setOnClickListener { presenter.onPausePressed() }
        binding.castPlayerSeekBack.setOnClickListener { presenter.onSeekBackPressed() }
        binding.castPlayerSeekForward.setOnClickListener { presenter.onSeekFwdPressed() }
        binding.castPlayerTrackLast.setOnClickListener { presenter.onTrackBackPressed() }
        binding.castPlayerTrackNext.setOnClickListener { presenter.onTrackFwdPressed() }
        binding.castPlayerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initMediaRouteButton() {
        chromeCastWrapper.initMediaRouteButton(binding.mediaRouteButton)
    }

    override fun setConnectionText(text: String) {
        binding.castPlayerStatusText.text = text
    }

    override fun setCurrentSecond(second: String) {
        binding.castPlayerCurrentTime.text = second
    }

    override fun setDuration(duration: String) {
        binding.castPlayerDuration.text = duration
    }

    override fun setPlaying() {
        binding.castPlayerPlay.isVisible = false
        binding.castPlayerPause.isVisible = true
        binding.castPlayerBuffering.isVisible = false
    }

    override fun setPaused() {
        binding.castPlayerPlay.isVisible = true
        binding.castPlayerPause.isVisible = false
        binding.castPlayerBuffering.isVisible = false
    }

    override fun setBuffering() {
        binding.castPlayerPlay.isVisible = false
        binding.castPlayerPause.isVisible = false
        binding.castPlayerBuffering.isVisible = true
    }

    override fun showMessage(msg: String) {
        Snackbar.make(view!!,msg,Snackbar.LENGTH_LONG).show()
    }

    override fun setTitle(title: String) {
        binding.castPlayerTitle.text = title
    }

    override fun updateSeekPosition(ratio: Float) {
        binding.castPlayerSeek.progress = (ratio * binding.castPlayerSeek.max).toInt()
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