package uk.co.sentinelweb.cuer.app.ui.play_control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.KoinContextHandler.get
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.CastPlayerViewBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class CastPlayerFragment() : Fragment(), CastPlayerContract.View {

    private val presenter: CastPlayerContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()
    private val res: ResourceWrapper by inject()

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding!!

    override val playerControls: CastPlayerContract.PlayerControls
        get() = presenter as CastPlayerContract.PlayerControls

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CastPlayerViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.castPlayerFab.setOnClickListener { presenter.onPlayPausePressed() }
        binding.castPlayerSeekBack.setOnClickListener { presenter.onSeekBackPressed() }
        binding.castPlayerSeekForward.setOnClickListener { presenter.onSeekFwdPressed() }
        binding.castPlayerSeekBack.setOnLongClickListener { presenter.onSeekBackSelectTimePressed() }
        binding.castPlayerSeekForward.setOnLongClickListener { presenter.onSeekSelectTimeFwdPressed() }
        binding.castPlayerTrackLast.setOnClickListener { presenter.onTrackBackPressed() }
        binding.castPlayerTrackNext.setOnClickListener { presenter.onTrackFwdPressed() }
        binding.castPlayerPlaylistText.setOnClickListener { presenter.onPlaylistClick() }
        binding.castPlayerImage.setOnClickListener { presenter.onPlaylistItemClick() }
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
        presenter.initialise()
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

    override fun setDurationColors(@ColorRes text: Int, @ColorRes upcomingBackground: Int) {
        binding.castPlayerDuration.setTextColor(res.getColor(text))
        binding.castPlayerDuration.setBackgroundColor(res.getColor(upcomingBackground))
    }

    override fun setDuration(duration: String) {
        binding.castPlayerDuration.text = duration
    }

    override fun setPlaying() {
        binding.castPlayerFab.setImageState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked), false)
        binding.castPlayerFab.showProgress(false)
    }

    override fun setPaused() {
        binding.castPlayerFab.setImageState(intArrayOf(android.R.attr.state_enabled), false)
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
        Glide.with(requireContext())
            .load(url)
            .into(binding.castPlayerImage)
    }

    override fun clearImage() {
        binding.castPlayerImage.setImageResource(0)
    }

    override fun setPlaylistName(name: String) {
        binding.castPlayerPlaylistText.text = name
    }

    override fun setPlaylistImage(url: String?) {
//        url?.apply {
//            Glide.with(requireContext())
//                .load(imageProvider.makeRef(this))
//                .into(binding.castPlayerPlaylistImage)
//        } ?: binding.castPlayerPlaylistImage.setImageResource(R.drawable.ic_nav_playlist_black)
    }

    override fun setSkipFwdText(text: String) {
        binding.castPlayerSkipfwdText.text = text
    }

    override fun setSkipBackText(text: String) {
        binding.castPlayerSkipbackText.text = text
    }

    override fun setSeekEnabled(enabled: Boolean) {
        binding.castPlayerSeek.isEnabled = enabled
    }

    override fun updateSeekPosition(ratio: Float) {
        binding.castPlayerSeek.progress = (ratio * binding.castPlayerSeek.max).toInt()
    }

    override fun navigate(navModel: NavigationModel) {
        (activity as NavigationProvider).navigate(navModel)
    }

    override fun makeItemTransitionExtras() =
        FragmentNavigatorExtras(
            binding.castPlayerTitle to TRANS_TITLE,
            binding.castPlayerImage to TRANS_IMAGE
        )

    companion object {
        val TRANS_IMAGE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_image) }
        val TRANS_TITLE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_title) }

        @JvmStatic
        val viewModule = module {
            scope(named<CastPlayerFragment>()) {
                scoped<CastPlayerContract.View> { getSource() }
                scoped<CastPlayerContract.Presenter> {
                    CastPlayerPresenter(
                        view = get(),
                        mapper = get(),
                        state = get(),
                        log = get(),
                        skipPresenter = get(),
                        res = get()
                    )
                }
                scoped {
                    SkipPresenter(
                        view = get(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = SkipContract.Mapper(timeSinceFormatter = get(), res = get()),
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                scoped<SkipContract.View> {
                    SkipView(
                        selectDialogCreator = SelectDialogCreator(
                            context = getSource<CastPlayerFragment>().requireContext()
                        )
                    )
                }
                scoped { CastPlayerUiMapper(get()) }
                viewModel { CastPlayerContract.State() }
            }
        }
    }
}