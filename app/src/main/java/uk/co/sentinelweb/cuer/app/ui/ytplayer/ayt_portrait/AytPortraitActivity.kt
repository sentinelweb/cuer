package uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_portrait

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.ActivityAytPortraitBinding
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.navigation.LinkNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.ribbon.RibbonModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.ui.play_control.mvi.CastPlayerMviFragment
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Content.DESCRIPTION
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Content.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.PlayerCommand.Play
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviFragment
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.CryptoLauncher
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.TimecodeDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

@ExperimentalCoroutinesApi
class AytPortraitActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<AytPortraitActivity>()

    private val playerController: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val playerFragment: CastPlayerMviFragment by inject()
    private val playlistFragment: PlaylistMviFragment by inject()
    private val navRouter: NavigationRouter by inject()
    private val itemLoader: PlayerContract.PlaylistItemLoader by inject()
    private val toast: ToastWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val aytViewHolder: AytViewHolder by inject()
    private val floatingService: FloatingPlayerServiceManager by inject()
    private val queueConsumer: QueueMediatorContract.Consumer by inject()
    private val cryptoLauncher: CryptoLauncher by inject()
    private val linkNavigator: LinkNavigator by inject()
    private val shareWrapper: AndroidShareWrapper by inject()
    private val multiPrefs: MultiPlatformPreferencesWrapper by inject()

    private lateinit var playerMviView: AytPortraitActivity.PlayerMviViewImpl

    private lateinit var binding: ActivityAytPortraitBinding

    private var currentItem: PlaylistAndItemDomain? = null

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        binding = ActivityAytPortraitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        castListener.listen()
    }

    override fun onStart() {
        super.onStart()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        edgeToEdgeWrapper.doOnApplyWindowInsets(binding.root) { view, insets, padding ->
            view.updatePadding(
                bottom = padding.bottom + insets.systemWindowInsetBottom
            )
        }
        // this POS restores the AYT player to the mvi kotlin state after returning from a home press
        if (this::playerMviView.isInitialized) {
            coroutines.mainScope.launch {
                // fixme check if this is still needed - delay added in PlayerStoreFactory.init()
                delay(50)
                playerMviView.dispatch(PlayerStateChanged(aytViewHolder.playerState))
            }
        }
    }

    override fun onStop() {
        // check to launch the floating player
        if (multiPrefs.playerAutoFloat
            && aytViewHolder.isPlaying
            && floatingService.hasPermission(this@AytPortraitActivity)
            && currentItem != null
            && !castListener.castStarting
            && !isChangingConfigurations
        ) {
            log.d("launch pip")
            aytViewHolder.switchView()
            aytViewHolder.processCommand(Play)
            floatingService.start(this@AytPortraitActivity, currentItem!!)
            finish()
        }
        super.onStop()
    }

    override fun onDestroy() {
        castListener.release()
        playerController.onViewDestroyed()
        playerController.onDestroy(aytViewHolder.willFinish())
        aytViewHolder.cleanupIfNotSwitching()
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (floatingService.isRunning()) {
            aytViewHolder.switchView()
            floatingService.stop()
        }
        log.d("onPostCreate")
        playerMviView = PlayerMviViewImpl(aytViewHolder)
        //playerFragment.initMediaRouteButton()
        playerController.onViewCreated(
            listOf(playerMviView, playerFragment.mviView),
            lifecycle.asEssentyLifecycle()
        )
        binding.portraitPlayerDescription.interactions = object : DescriptionContract.Interactions {

            override fun onLinkClick(link: LinkDomain.UrlLinkDomain) {
                playerMviView.dispatch(LinkClick(link))
            }

            override fun onChannelClick() {
                playerMviView.dispatch(ChannelClick)
            }

            override fun onCryptoClick(cryptoAddress: LinkDomain.CryptoLinkDomain) {
                cryptoLauncher.launch(cryptoAddress)// todo route mvi
            }

            override fun onTimecodeClick(timecode: TimecodeDomain) {
                playerMviView.dispatch(OnSeekToPosition(timecode.position))
            }

            override fun onSelectPlaylistChipClick(model: ChipModel) = Unit

            override fun onRemovePlaylist(chipModel: ChipModel) = Unit

            override fun onRibbonItemClick(ribbonItem: RibbonModel) = when (ribbonItem.type) {
                RibbonModel.Type.STAR -> playerMviView.dispatch(StarClick)
                RibbonModel.Type.UNSTAR -> playerMviView.dispatch(StarClick)
                RibbonModel.Type.SHARE -> playerMviView.dispatch(ShareClick)
                RibbonModel.Type.SUPPORT -> playerMviView.dispatch(Support)
                RibbonModel.Type.FULL -> playerMviView.dispatch(FullScreenClick)
                RibbonModel.Type.PIP -> playerMviView.dispatch(PipClick)
                RibbonModel.Type.LIKE -> playerMviView.dispatch(OpenClick)
                RibbonModel.Type.COMMENT -> playerMviView.dispatch(OpenClick)
                RibbonModel.Type.LAUNCH -> playerMviView.dispatch(OpenClick)
                else -> log.e(
                    "Unsupported ribbon action",
                    IllegalStateException("Unsupported ribbon action: $ribbonItem")
                )
            }

            override fun onPlaylistChipClick(chipModel: ChipModel) = Unit
        }
        val playlistAndItem = itemLoader.load()
            ?: queueConsumer.playlistAndItem
            ?: throw IllegalArgumentException("Could not get playlist item")

        playlistFragment.arguments = bundleOf(
            HEADLESS.name to true,
            SOURCE.name to playlistAndItem.playlistId?.source.toString(),
            PLAYLIST_ID.name to (playlistAndItem.playlistId?.id?.value),
            PLAYLIST_ITEM_ID.name to (playlistAndItem.item.id?.id?.value),
        )
        log.d("onPostCreate")
    }

    // region MVI view
    inner class PlayerMviViewImpl(aytViewHolder: AytViewHolder) :
        BaseMviView<PlayerContract.View.Model, PlayerContract.View.Event>(),
        PlayerContract.View {

        init {
            aytViewHolder.addView(this@AytPortraitActivity, binding.playerContainer, this, false)
        }

        override val renderer: ViewRenderer<PlayerContract.View.Model> = diff {
            diff(get = PlayerContract.View.Model::description, set = {
                log.d("set description")
                binding.portraitPlayerDescription.setModel(it)
            })
            diff(get = PlayerContract.View.Model::playlistAndItem, set = {
                currentItem = it
                it?.item?.media?.also {
                    binding.portraitPlayerDescription.ribbonItems
                        .find { it.item.type == RibbonModel.Type.STAR }
                        ?.isVisible = !it.starred
                    binding.portraitPlayerDescription.ribbonItems
                        .find { it.item.type == RibbonModel.Type.UNSTAR }
                        ?.isVisible = it.starred
                }
            })
            diff(get = PlayerContract.View.Model::content, set = {
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
                is Command -> label.command
                    .let { aytViewHolder.processCommand(it) }
                    .also {
                        if (label.command == Play) {
                            edgeToEdgeWrapper.setDecorFitsSystemWindows(this@AytPortraitActivity)
                        }
                    }

                is LinkOpen -> linkNavigator.navigateLink(label.link)

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
                    NavigationModel(YOUTUBE_VIDEO_POS, mapOf(PLAYLIST_ITEM to label.item))
                )

                is Share -> shareWrapper.share(label.item.media)
            }
        }
    }

    fun trackChange(item: PlaylistItemDomain, start: Boolean) {
        playerMviView.dispatch(TrackClick(item, start))
    }

    companion object {

        fun start(c: Context, playlistAndItem: PlaylistAndItemDomain) = c.startActivity(
            Intent(c, AytPortraitActivity::class.java).apply {
                putExtra(PLAYLIST_AND_ITEM.toString(), playlistAndItem.serialise())
            })

        fun startFromService(c: Context, playlistAndItem: PlaylistAndItemDomain) = c.startActivity(
            Intent(c, AytPortraitActivity::class.java).apply {
                putExtra(PLAYLIST_AND_ITEM.toString(), playlistAndItem.serialise())
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
    }


}