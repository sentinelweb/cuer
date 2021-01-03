package uk.co.sentinelweb.cuer.app.ui.play_control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.cast_player_view.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.CastPlayerViewBinding
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider

class CastPlayerFragment() : Fragment(), CastPlayerContract.View {

    private val presenter: CastPlayerContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding!!

    override val playerControls: CastPlayerContract.PlayerControls
        get() = presenter as CastPlayerContract.PlayerControls

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        presenter.initialise()
        _binding = CastPlayerViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.castPlayerFab.setOnClickListener { presenter.onPlayPausePressed() }
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
        presenter.onDestroyView()
        _binding = null
    }

    override fun initMediaRouteButton() {
        chromeCastWrapper.initMediaRouteButton(binding.mediaRouteButton)
    }

    override fun setCurrentSecond(second: String) {
        binding.castPlayerCurrentTime.text = second
    }

    override fun setDuration(duration: String) {
        binding.castPlayerDuration.text = duration
    }

    override fun setPlaying() {
        binding.castPlayerFab.setImageResource(R.drawable.ic_player_pause_black)
        binding.castPlayerFab.showProgress(false)
    }

    override fun setPaused() {
        binding.castPlayerFab.setImageResource(R.drawable.ic_player_play_black)
        binding.castPlayerFab.showProgress(false)
    }

    override fun showBuffering() {
        binding.castPlayerFab.showProgress(true)
    }

    override fun hideBuffering() {
        binding.castPlayerFab.showProgress(false)
    }

    override fun showMessage(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }

    override fun setTitle(title: String) {
        binding.castPlayerTitle.text = title
    }

    override fun setImage(url: String) {
        Glide.with(binding.castPlayerImage)
            .load(url)
            .into(binding.castPlayerImage)
    }

    override fun clearImage() {
        binding.castPlayerImage.setImageResource(0)
    }

    override fun setPlaylistName(name: String) {
        cast_player_playlist_text.text = name
    }

    override fun setPlaylistImage(url: String?) {
        url?.apply {
            Glide.with(binding.castPlayerPlaylistImage)
                .load(imageProvider.makeRef(this))
                .into(binding.castPlayerPlaylistImage)
        } ?: binding.castPlayerPlaylistImage.setImageResource(R.drawable.ic_nav_playlist_black)
    }

    override fun updateSeekPosition(ratio: Float) {
        binding.castPlayerSeek.progress = (ratio * binding.castPlayerSeek.max).toInt()
    }

    companion object {
        @JvmStatic
        val viewModule = module {
            scope(named<CastPlayerFragment>()) {
                scoped<CastPlayerContract.View> { getSource() }
                scoped<CastPlayerContract.Presenter> {
                    CastPlayerPresenter(
                        get(),
                        get(),
                        get(),
                        get()
                    )
                }
                scoped { CastPlayerUiMapper(get()) }
                viewModel { CastPlayerContract.State() }
            }
        }
    }
}