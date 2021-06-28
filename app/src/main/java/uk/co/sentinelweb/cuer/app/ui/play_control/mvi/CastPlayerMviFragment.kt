package uk.co.sentinelweb.cuer.app.ui.play_control.mvi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.youtube.player.YouTubePlayer
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.context.GlobalContext.get
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.CastPlayerViewBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class CastPlayerMviFragment() :
    Fragment(),
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()

    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()
    private val res: ResourceWrapper by inject()

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var mviView: CastPlayerViewImpl

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
        binding.castPlayerFab.setOnClickListener { mviView.dispatch(Event.PlayPauseClicked(true)) }// todo get playstate
        binding.castPlayerSeekBack.setOnClickListener { mviView.dispatch(Event.SkipBackClicked) }
        binding.castPlayerSeekForward.setOnClickListener { mviView.dispatch(Event.SkipFwdClicked) }
        binding.castPlayerSeekBack.setOnLongClickListener { mviView.dispatch(Event.SkipBackSelectClicked); true }
        binding.castPlayerSeekForward.setOnLongClickListener { mviView.dispatch(Event.SkipFwdSelectClicked); true }
        binding.castPlayerTrackLast.setOnClickListener { mviView.dispatch(Event.TrackBackClicked) }
        binding.castPlayerTrackNext.setOnClickListener { mviView.dispatch(Event.TrackFwdClicked) }
        binding.castPlayerPlaylistText.setOnClickListener { mviView.dispatch(Event.PlaylistClicked) }
        binding.castPlayerImage.setOnClickListener { mviView.dispatch(Event.ItemClicked) }
        binding.castPlayerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // todo update time ui
                }
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //presenter.onSeekFinished()
                mviView.dispatch(Event.SeekBarChanged(seekBar.progress / seekBar.max.toFloat()));
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    fun initMediaRouteButton() {
        chromeCastWrapper.initMediaRouteButton(binding.mediaRouteButton)
    }

    fun setCurrentSecond(second: String) {
        binding.castPlayerCurrentTime.text = second
    }

    fun setDurationColors(@ColorRes text: Int, @ColorRes upcomingBackground: Int) {
        binding.castPlayerDuration.setTextColor(res.getColor(text))
        binding.castPlayerDuration.setBackgroundColor(res.getColor(upcomingBackground))
    }

    fun setDuration(duration: String) {
        binding.castPlayerDuration.text = duration
    }

    fun setPlaying() {
        binding.castPlayerFab.setImageState(intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked), false)
        binding.castPlayerFab.showProgress(false)
    }

    fun setPaused() {
        binding.castPlayerFab.setImageState(intArrayOf(android.R.attr.state_enabled), false)
        binding.castPlayerFab.showProgress(false)
    }

    fun showBuffering() {
        binding.castPlayerFab.showProgress(true)
    }

    fun hideBuffering() {
        binding.castPlayerFab.showProgress(false)
    }

    fun showMessage(msg: String) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }

    fun setTitle(title: String) {
        binding.castPlayerTitle.text = title
    }

    fun setImage(url: String) {
        Glide.with(requireContext())
            .load(url)
            .into(binding.castPlayerImage)
    }

    fun clearImage() {
        binding.castPlayerImage.setImageResource(0)
    }

    fun setPlaylistName(name: String) {
        binding.castPlayerPlaylistText.text = name
    }

    fun setPlaylistImage(url: String?) {
//        url?.apply {
//            Glide.with(requireContext())
//        .loadFirebaseOrOtherUrl(this, imageProvider)
//                .into(binding.castPlayerPlaylistImage)
//        } ?: binding.castPlayerPlaylistImage.setImageResource(R.drawable.ic_nav_playlist_black)
    }

    fun setSkipFwdText(text: String) {
        binding.castPlayerSkipfwdText.text = text
    }

    fun setSkipBackText(text: String) {
        binding.castPlayerSkipbackText.text = text
    }

    fun setSeekEnabled(enabled: Boolean) {
        binding.castPlayerSeek.isEnabled = enabled
    }

    fun setState(state: PlayerStateDomain?) {
        binding.castPlayerCurrentState.text = state?.toString()
    }

    fun updateSeekPosition(ratio: Float) {
        binding.castPlayerSeek.progress = (ratio * binding.castPlayerSeek.max).toInt()
    }

    fun navigate(navModel: NavigationModel) {
        (activity as NavigationProvider).navigate(navModel)
    }

    fun makeItemTransitionExtras() =
        FragmentNavigatorExtras(
            binding.castPlayerTitle to TRANS_TITLE,
            binding.castPlayerImage to TRANS_IMAGE
        )

    // region MVI view
    inner class CastPlayerViewImpl constructor(
        private val player: YouTubePlayer
    ) : BaseMviView<Model, Event>(),
        PlayerContract.View {
        init {

        }

        override fun render(model: Model) {

        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {

        }
    }
    // endreigon

    companion object {
        val TRANS_IMAGE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_image) }
        val TRANS_TITLE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_title) }

        @JvmStatic
        val viewModule = module {
            scope(named<CastPlayerMviFragment>()) {

            }

        }
    }

}