package uk.co.sentinelweb.cuer.app.ui.play_control

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.CastPlayerViewBinding
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.play.PlayDialog
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerContract.DurationStyle.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.CastConnectionState.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.ControlTarget.*
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.chromecast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageProvider
import uk.co.sentinelweb.cuer.app.util.image.loadFirebaseOrOtherUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class CastPlayerFragment() :
    Fragment(),
    CastPlayerContract.View,
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<CastPlayerFragment>()
    private val presenter: CastPlayerContract.Presenter by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val imageProvider: ImageProvider by inject()
    private val res: ResourceWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val log: LogWrapper by inject()
    private val castController: CastController by scope.inject()

    init {
        log.tag(this)
    }

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("CastPlayerViewBinding not bound")

    override val playerControls: PlayerContract.PlayerControls
        get() = presenter as PlayerContract.PlayerControls

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
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
        binding.castPlayerSupport.setOnClickListener { presenter.onSupport() }
        binding.castButton.setOnClickListener { presenter.onCastClick() }
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

    override fun showSupport(media: MediaDomain) {
        SupportDialogFragment.show(requireActivity(), media)
    }

    override fun setNextTrackEnabled(nextTrackEnabled: Boolean) {
        binding.castPlayerTrackNext.isEnabled = nextTrackEnabled
    }

    override fun setPrevTrackEnabled(prevTrackEnabled: Boolean) {
        binding.castPlayerTrackLast.isEnabled = prevTrackEnabled
    }

    override fun setCastDetails(details: CastPlayerContract.State.CastDetails) {
        when (details.target) {
            None -> binding.castButton.setImageResource(R.drawable.ic_chromecast)
            ChromeCast -> when (details.connectionState) {
                Connected -> binding.castButton.setImageResource(R.drawable.ic_chromecast_connected)
                Connecting, Disconnected -> binding.castButton.setImageResource(R.drawable.ic_chromecast)
            }

            CuerCast -> when (details.connectionState) {
                Connected -> binding.castButton.setImageResource(R.drawable.ic_cuer_cast_connected)
                Connecting, Disconnected -> binding.castButton.setImageResource(R.drawable.ic_cuer_cast)
            }

            FloatingWindow -> binding.castButton.setImageResource(R.drawable.ic_picture_in_picture)
        }
        when (details.connectionState) {
            Connected, Disconnected -> hideBuffering()
            Connecting -> showBuffering()
        }
        binding.castConnectionSummary.text = details.name
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
        // should be a better way to inject castController but seems to be a lot of circular refs
        presenter.setCastController(castController)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

//    override fun initMediaRouteButton() {
//        chromeCastWrapper.initMediaRouteButton(binding.mediaRouteButton)
//    }

    override fun setPosition(second: String) {
        binding.castPlayerPosition.text = second
    }

    override fun setLiveTime(second: String?) {
        binding.castPlayerLiveTime.isVisible = second != null
        second?.apply { binding.castPlayerLiveTime.text = second }
    }

    override fun setDurationStyle(style: CastPlayerContract.DurationStyle) = when (style) {
        Normal -> {
            binding.castPlayerDuration.setTextColor(res.getColor(R.color.text_primary))
            binding.castPlayerDuration.setBackgroundColor(res.getColor(R.color.transparent))
        }

        Upcoming -> {
            binding.castPlayerDuration.setTextColor(res.getColor(R.color.white))
            binding.castPlayerDuration.setBackgroundColor(res.getColor(R.color.upcoming_background))
            binding.castPlayerDuration.text = getString(R.string.upcoming)
        }

        Live -> {
            binding.castPlayerDuration.setTextColor(res.getColor(R.color.white))
            binding.castPlayerDuration.setBackgroundColor(res.getColor(R.color.live_background))
            binding.castPlayerDuration.text = getString(R.string.live)
        }
    }

    override fun setDuration(duration: String) {
        binding.castPlayerDuration.text = duration
    }

    override fun setPlaying() {
        binding.castPlayerFab.setImageState(
            intArrayOf(
                android.R.attr.state_enabled,
                android.R.attr.state_checked
            ), false
        )
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
            .loadFirebaseOrOtherUrl(url, imageProvider)
            .transition(DrawableTransitionOptions.withCrossFade())
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
//        .loadFirebaseOrOtherUrl(this, imageProvider)
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
        //log.e("seekbar enabled: $enabled", Exception())
        binding.castPlayerSeek.isEnabled = enabled
    }

    override fun setState(state: PlayerStateDomain?) {
        binding.castPlayerCurrentState.text = state?.toString()
    }

    override fun updateSeekPosition(ratio: Float) {
        binding.castPlayerSeek.progress = (ratio * binding.castPlayerSeek.max).toInt()
    }

    override fun navigate(navModel: NavigationModel) {
        navigationProvider.navigate(navModel)
    }

    /*override*/ fun makeItemTransitionExtras() =
        FragmentNavigatorExtras(
            binding.castPlayerTitle to TRANS_TITLE,
            binding.castPlayerImage to TRANS_IMAGE
        )

    companion object {
        val TRANS_IMAGE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_image) }
        val TRANS_TITLE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_title) }

        @JvmStatic
        val viewModule = module {
            factory { CompactPlayerScroll() }
            scope(named<CastPlayerFragment>()) {
                scoped { CastPlayerContract.State() } // was viewModel
                scoped<CastPlayerContract.View> { get<CastPlayerFragment>() }
                scoped<CastPlayerContract.Presenter> {
                    CastPlayerPresenter(
                        view = get(),
                        mapper = get(),
                        state = get(),
                        log = get(),
                        skipControl = get(),
                        playUseCase = get(),
                        playlistAndItemMapper = get(),
                    )
                }
                scoped<SkipContract.External> {
                    SkipPresenter(
                        view = get(),
                        state = SkipContract.State(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get()
                    )
                }
                scoped<SkipContract.View> {
                    SkipView(
                        selectDialogCreator = SelectDialogCreator(context = this.getFragmentActivity())
                    )
                }
                scoped { CastPlayerUiMapper(get(), get(), get()) }

                // todo play usecase - extract
                scoped<PlatformLaunchWrapper> { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                // fixme needed for play dialog - but shouldn't be needed - remove
                scoped { navigationRouter(false, this.getFragmentActivity(), false) }
                scoped {
                    PlayUseCase(
                        queue = get(),
                        ytCastContextHolder = get(),
                        prefsWrapper = get(),
                        coroutines = get(),
                        floatingService = get(),
                        playDialog = get(),
                        strings = get(),
                        cuerCastPlayerWatcher = get()
                    )
                }
                scoped<PlayUseCase.Dialog> {
                    PlayDialog(
                        get<CastPlayerFragment>(),
                        itemFactory = get(),
                        itemModelMapper = get(),
                        navigationRouter = get(),
                        castDialogWrapper = get(),
                        floatingService = get(),
                        log = get(),
                        alertDialogCreator = get(),
                        youtubeApi = get(),
                    )
                }
                scoped { ItemFactory(get(), get(), get()) }
                scoped { ItemModelMapper(get(), get(), get(), get()) }

            }
        }
    }

}