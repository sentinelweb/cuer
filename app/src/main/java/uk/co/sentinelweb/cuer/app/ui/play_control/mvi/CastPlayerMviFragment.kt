package uk.co.sentinelweb.cuer.app.ui.play_control.mvi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
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
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlayerStateDomain

class CastPlayerMviFragment() :
    Fragment(),
    AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<CastPlayerMviFragment>()

    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val imageProvider: FirebaseImageProvider by inject()
    private val res: ResourceWrapper by inject()

    private var _binding: CastPlayerViewBinding? = null
    private val binding get() = _binding!!

    private lateinit var _mviView: CastPlayerViewImpl
    val mviView: CastPlayerViewImpl
        get() = _mviView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CastPlayerViewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _mviView = CastPlayerViewImpl()
        binding.castPlayerFab.setOnClickListener { _mviView.dispatch(Event.PlayPauseClicked()) }// todo get playstate
        binding.castPlayerSeekBack.setOnClickListener { _mviView.dispatch(Event.SkipBackClicked) }
        binding.castPlayerSeekForward.setOnClickListener { _mviView.dispatch(Event.SkipFwdClicked) }
        binding.castPlayerSeekBack.setOnLongClickListener { _mviView.dispatch(Event.SkipBackSelectClicked); true }
        binding.castPlayerSeekForward.setOnLongClickListener { _mviView.dispatch(Event.SkipFwdSelectClicked); true }
        binding.castPlayerTrackLast.setOnClickListener { _mviView.dispatch(Event.TrackBackClicked) }
        binding.castPlayerTrackNext.setOnClickListener { _mviView.dispatch(Event.TrackFwdClicked) }
        binding.castPlayerPlaylistText.setOnClickListener { _mviView.dispatch(Event.PlaylistClicked) }
        binding.castPlayerImage.setOnClickListener { _mviView.dispatch(Event.ItemClicked) }
        binding.castPlayerSupport.setOnClickListener { _mviView.dispatch(Event.Support) }
        binding.castPlayerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                if (fromUser) {
//                    // todo update time ui
//                }
            }

            override fun onStartTrackingTouch(view: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                _mviView.dispatch(Event.SeekBarChanged(seekBar.progress / seekBar.max.toFloat()))
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

    fun setPosition(second: String) {
        binding.castPlayerPosition.text = second
    }

    fun setLiveTime(second: String?) {
        binding.castPlayerLiveTime.isVisible = second != null
        second?.apply { binding.castPlayerLiveTime.text = second }
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

    // region MVI view
    inner class CastPlayerViewImpl constructor(
    ) : BaseMviView<Model, Event>(),
        PlayerContract.View {

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::playState, set = {
                when (it) {
                    PlayerStateDomain.BUFFERING -> showBuffering()
                    PlayerStateDomain.PLAYING -> setPlaying()
                    PlayerStateDomain.PAUSED -> setPaused()
                    else -> Unit
                }
            })
            diff(get = Model::itemImage, set = { url ->
                url?.let { setImage(it) }
            })
            diff(get = Model::texts, set = { texts ->
                texts.title?.let { setTitle(it) }
                texts.playlistTitle?.let { setPlaylistName(it) }
                texts.playlistTitle?.let { setPlaylistName(it) }
                texts.skipFwdText?.let { setSkipFwdText(it) }
                texts.skipBackText?.let { setSkipBackText(it) }
            })
            diff(get = Model::times, set = { times ->
                updateSeekPosition(times.seekBarFraction)
                setPosition(times.positionText)
                setLiveTime(times.liveTime)
                if (times.isLive) {
                    setDurationColors(R.color.white, R.color.live_background)
                    setDuration(res.getString(R.string.live))
                    updateSeekPosition(0f)
                } else {
                    setDurationColors(R.color.text_primary, R.color.transparent)
                    setDuration(times.durationText)
                    updateSeekPosition(times.seekBarFraction)
                }
                //setSeekEnabled(!times.isLive)
            })
            diff(get = Model::buttons, set = { buttons ->
                binding.castPlayerTrackNext.isEnabled = buttons.nextTrackEnabled
                binding.castPlayerTrackLast.isEnabled = buttons.prevTrackEnabled
                binding.castPlayerSeek.isEnabled = buttons.seekEnabled
            })
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) = Unit
    }
    // endreigon

    companion object {
        val TRANS_IMAGE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_image) }
        val TRANS_TITLE by lazy { get().get<ResourceWrapper>().getString(R.string.cast_player_trans_title) }

        @JvmStatic
        val fragmentModule = module {
            scope(named<CastPlayerMviFragment>()) {

            }
        }
    }

}