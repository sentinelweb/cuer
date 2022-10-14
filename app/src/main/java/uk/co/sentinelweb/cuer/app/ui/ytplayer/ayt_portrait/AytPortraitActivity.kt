package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.ActivityAytPortraitBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Screen.DESCRIPTION
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Screen.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.CryptoLauncher
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.TimecodeDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

@ExperimentalCoroutinesApi
class AytPortraitActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<AytPortraitActivity>()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val playerFragment: CastPlayerMviFragment by inject()
    private val playlistFragment: PlaylistFragment by inject()
    private val navRouter: NavigationRouter by inject()
    private val itemLoader: PlayerContract.PlaylistItemLoader by inject()
    private val toast: ToastWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val aytViewHolder: AytViewHolder by inject()
    private val floatingService: FloatingPlayerServiceManager by inject()
    private val queueConsumer: QueueMediatorContract.Consumer by inject()
    private val cryptoLauncher: CryptoLauncher by inject()
    private val linkScanner: LinkScanner by inject()
    private val shareWrapper: ShareWrapper by inject()

    private lateinit var mviView: AytPortraitActivity.MviViewImpl
    private lateinit var binding: ActivityAytPortraitBinding

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAytPortraitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        castListener.listen()
    }

    override fun onDestroy() {
        castListener.release()
        controller.onViewDestroyed()
        controller.onDestroy(aytViewHolder.willFinish())
        aytViewHolder.cleanupIfNotSwitching()
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (floatingService.isRunning()) {
            aytViewHolder.switchView()
            floatingService.stop()
        }
        mviView = MviViewImpl(aytViewHolder)
        playerFragment.initMediaRouteButton()
        controller.onViewCreated(
            listOf(mviView, playerFragment.mviView),
            lifecycle.asEssentyLifecycle()
        )

        binding.portraitPlayerDescription.interactions = object : DescriptionContract.Interactions {

            override fun onLinkClick(link: LinkDomain.UrlLinkDomain) {
                mviView.dispatch(LinkClick(link.address))
            }

            override fun onChannelClick() {
                mviView.dispatch(ChannelClick)
            }

            override fun onCryptoClick(cryptoAddress: LinkDomain.CryptoLinkDomain) {
                cryptoLauncher.launch(cryptoAddress)
            }

            override fun onTimecodeClick(timecode: TimecodeDomain) {
                mviView.dispatch(OnSeekToPosition(timecode.position))
            }

            override fun onSelectPlaylistChipClick(model: ChipModel) = Unit

            override fun onRemovePlaylist(chipModel: ChipModel) = Unit

            override fun onRibbonItemClick(ribbonItem: RibbonModel) = when (ribbonItem.type) {
                RibbonModel.Type.STAR -> mviView.dispatch(StarClick)
                RibbonModel.Type.UNSTAR -> mviView.dispatch(StarClick)
                RibbonModel.Type.SHARE -> mviView.dispatch(ShareClick)
                RibbonModel.Type.SUPPORT -> mviView.dispatch(Support)
                RibbonModel.Type.FULL -> mviView.dispatch(FullScreenClick)
                RibbonModel.Type.PIP -> mviView.dispatch(PipClick)
                RibbonModel.Type.LIKE -> mviView.dispatch(OpenClick)
                RibbonModel.Type.COMMENT -> mviView.dispatch(OpenClick)
                RibbonModel.Type.LAUNCH -> mviView.dispatch(OpenClick)
                else -> log.e(
                    "Unsupported ribbon action",
                    IllegalStateException("Unsupported ribbon action: $ribbonItem")
                )
            }

        }
        val playlistItem = itemLoader.load()
            ?: queueConsumer.currentItem
            ?: throw IllegalArgumentException("Could not get playlist item")
        playlistFragment.arguments = bundleOf(
            HEADLESS.name to true,
            SOURCE.name to OrchestratorContract.Source.LOCAL.toString(),
            PLAYLIST_ID.name to (playlistItem.playlistId),
            PLAYLIST_ITEM_ID.name to (playlistItem.id),
        )
        playlistFragment.external.interactions = playlistInteractions
    }

    // region MVI view
    inner class MviViewImpl(aytViewHolder: AytViewHolder) :
        BaseMviView<Model, Event>(),
        PlayerContract.View {

        init {
            aytViewHolder.addView(this@AytPortraitActivity, binding.playerContainer, this)
        }

        override val renderer: ViewRenderer<Model> = diff {
            diff(get = Model::description, set = {
                log.d("set description")
                binding.portraitPlayerDescription.setModel(it)
            })
            diff(get = Model::playlistItem, set = {
                it?.media?.also {
                    binding.portraitPlayerDescription.ribbonItems
                        .find { it.item.type == RibbonModel.Type.STAR }
                        ?.isVisible = !it.starred
                    binding.portraitPlayerDescription.ribbonItems
                        .find { it.item.type == RibbonModel.Type.UNSTAR }
                        ?.isVisible = it.starred
                }
            })
            diff(get = Model::screen, set = {
                when (it) {
                    DESCRIPTION -> {
                        binding.portraitPlayerDescription.isVisible = true
                        binding.portraitPlayerPlaylist.isVisible = false
                    }

                    PLAYLIST -> {
                        binding.portraitPlayerDescription.isVisible = false
                        binding.portraitPlayerPlaylist.isVisible = true
                    }
                    else -> Unit
                }
            })
        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
            when (label) {
                is Command -> label.command.let { aytViewHolder.processCommand(it) }
                is LinkOpen ->
                    navRouter.navigate(
                        linkScanner.scan(label.url)?.let { scanned ->
                            when (scanned.first) {
                                ObjectTypeDomain.MEDIA -> navShare(label.url)
                                ObjectTypeDomain.PLAYLIST -> navShare(label.url)
                                ObjectTypeDomain.PLAYLIST_ITEM -> navShare(label.url)
                                ObjectTypeDomain.CHANNEL -> navLink(label.url)
                                else -> navLink(label.url)
                            }
                        } ?: navLink(label.url)
                    )
                is ChannelOpen ->
                    label.channel.platformId?.let { id ->
                        navRouter.navigate(
                            NavigationModel(YOUTUBE_CHANNEL, mapOf(CHANNEL_ID to id))
                        )
                    }
                is FullScreenPlayerOpen -> label.also {
                    aytViewHolder.switchView()
                    navRouter.navigate(
                        NavigationModel(LOCAL_PLAYER_FULL, mapOf(PLAYLIST_ITEM to it.item))
                    )
                    finish()
                }
                is PipPlayerOpen -> {
                    val hasPermission = floatingService.hasPermission(this@AytPortraitActivity)
                    if (hasPermission) {
                        aytViewHolder.switchView()
                    }
                    floatingService.start(this@AytPortraitActivity, label.item)
                    if (hasPermission) {
                        finish()
                    }
                }

                is PortraitPlayerOpen -> toast.show("Already in portrait mode - shouldn't get here")
                is ShowSupport -> SupportDialogFragment.show(
                    this@AytPortraitActivity,
                    label.item.media
                )

                is ItemOpen -> navRouter.navigate(
                    NavigationModel(
                        YOUTUBE_VIDEO,
                        mapOf(NavigationModel.Param.PLATFORM_ID to label.item.media.platformId)
                    )
                )

                is Share -> shareWrapper.share(label.item.media)
            }
        }

        private fun navLink(link: String) = NavigationModel(WEB_LINK, mapOf(LINK to link))

        private fun navShare(link: String) = NavigationModel(SHARE, mapOf(LINK to link))
    }

    private val playlistInteractions = object : PlaylistContract.Interactions {

        override fun onPlayStartClick(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, true))
        }

        override fun onRelated(item: PlaylistItemDomain) {

        }

        override fun onView(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, false))
        }

        override fun onPlay(item: PlaylistItemDomain) {
            mviView.dispatch(TrackClick(item, false))
        }

    }

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, AytPortraitActivity::class.java).apply {
                putExtra(PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }


}