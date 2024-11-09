/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.sentinelweb.cuer.app.ui.exoplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Surface
import androidx.fragment.app.FragmentActivity
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.deepOptions
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.MemoryPlaylist.QueueTemp
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogContract
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.SelectDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.support.SupportDialogFragment
import uk.co.sentinelweb.cuer.app.ui.common.navigation.LinkNavigator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipContract
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipPresenter
import uk.co.sentinelweb.cuer.app.ui.common.skip.SkipView
import uk.co.sentinelweb.cuer.app.ui.exoplayer.ExoPlayerComposebles.ExoPlayerUi
import uk.co.sentinelweb.cuer.app.ui.exoplayer.ExoPlayerComposebles.getKoin
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.*
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model.Companion.Initial
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.ui.player.PlayerStoreFactory
import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ItemLoader
import uk.co.sentinelweb.cuer.app.ui.ytplayer.LocalPlayerCastListener
import uk.co.sentinelweb.cuer.app.ui.ytplayer.PlayerModule
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ShowHideUi
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land.AytLandActivity
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.chromecast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ExoActivity : FragmentActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<AytLandActivity>()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val toast: ToastWrapper by inject()
    private val res: ResourceWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val floatingService: FloatingPlayerServiceManager by inject()

    private var currentItem: PlaylistItemDomain? = null

    private lateinit var mviView: MviViewImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castListener.listen()
        mviView = MviViewImpl()
        enableEdgeToEdge()
        setContent {
            Surface {
                ExoPlayerUi(mviView)
            }
        }
    }

    override fun onDestroy() {
        castListener.release()
        controller.onViewDestroyed()
        controller.onDestroy(true)
        super.onDestroy()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (floatingService.isRunning()) {
            floatingService.stop()
        }

        controller.onViewCreated(
            listOf(mviView),
            lifecycle.asEssentyLifecycle()
        )
    }

    // region MVI view
    inner class MviViewImpl :
        BaseMviView<Model, Event>(),
        PlayerContract.View {

        private val _model = MutableStateFlow<Model>(Initial)
        val model = _model.asStateFlow()

        override val renderer: ViewRenderer<Model> = object : ViewRenderer<Model> {
            override fun render(model: Model) {
                _model.value = model
            }

        }
//        override val renderer: ViewRenderer<Model> = diff {
//            diff(get = Model::playState, set = {
//                when (it) {
//                    BUFFERING -> Unit //controlsBinding.controlsPlayFab.showProgress(true)
//                    PLAYING -> {
////                        updatePlayingIcon(true)
////                        controlsBinding.controlsPlayFab.showProgress(false)
//                    }
//
//                    PAUSED -> {
////                        updatePlayingIcon(false)
////                        controlsBinding.controlsPlayFab.showProgress(false)
//                    }
//
//                    else -> Unit
//                }
//            })
//            diff(get = Model::playlistItem, set = {
//                currentItem = it
//            })
//            diff(get = Model::texts, set = { texts ->
////                controlsBinding.apply {
////                    controlsVideoTitle.text = texts.title
////                    controlsVideoPlaylist.text = texts.playlistTitle
////                    controlsVideoPlaylistData.text = texts.playlistData
////                    controlsTrackLastText.text = texts.lastTrackText
////                    controlsTrackLastText.isVisible = texts.lastTrackText != ""
////                    controlsTrackNextText.text = texts.nextTrackText
////                    controlsTrackNextText.isVisible = texts.nextTrackText != ""
////                    controlsSkipfwdText.text = texts.skipFwdText
////                    controlsSkipbackText.text = texts.skipBackText
////                }
//            })
//            diff(get = Model::times, set = { times ->
////                controlsBinding.apply {
////                    controlsSeek.progress = (times.seekBarFraction * controlsSeek.max).toInt()
////                    controlsCurrentTime.text = times.positionText
////
////                    if (times.isLive) {
////                        controlsDuration.setBackgroundColor(res.getColor(R.color.live_background))
////                        controlsDuration.text = res.getString(R.string.live)
////                        controlsSeek.isEnabled = false
////                    } else {
////                        controlsDuration.setBackgroundColor(res.getColor(R.color.info_text_overlay_background))
////                        controlsDuration.text = times.durationText
////                        controlsSeek.isEnabled = true
////                    }
////                }
//            })
//        }

        override suspend fun processLabel(label: Label) {
            when (label) {
                is Command -> label.command.let { command ->
                    //aytViewHolder.processCommand(command)
                }

                is ChannelOpen -> Unit // todo open folder
//                    label.channel.platformId?.let { id ->
//                        navRouter.navigate(
//                            NavigationModel(
//                                YOUTUBE_CHANNEL,
//                                mapOf(CHANNEL_ID to id)
//                            )
//                        )
//                    }

                is FullScreenPlayerOpen -> toast.show("Already in landscape mode - shouldn't get here")
                is PipPlayerOpen -> {
                    val hasPermission = floatingService.hasPermission(this@ExoActivity)
//                    if (hasPermission) {
//                        aytViewHolder.switchView()
//                    }
                    floatingService.start(this@ExoActivity, label.item)
                    if (hasPermission) {
                        finishAffinity()
                    }
                }

                is PortraitPlayerOpen -> label.also {
                    //aytViewHolder.switchView()
                    //navRouter.navigate(NavigationModel(LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it.item)))
                    finish()
                }

                is ShowSupport -> SupportDialogFragment.show(this@ExoActivity, label.item.media)

                is Stop -> finish()
                else -> Unit
            }
        }
    }


    companion object {

        // todo cleanup
        fun start(c: Context, playlistAndItem: PlaylistAndItemDomain) =
            CoroutineScope(Dispatchers.Main).launch {
                val use: PlaylistAndItemDomain = if (playlistAndItem.playlistId==null) {
                    val queuePlaylist = PlaylistDomain(
                        id = Identifier(QueueTemp.id, MEMORY),
                        title = "Empty",
                        items = listOf(playlistAndItem.item)
                    )

                    getKoin().get<PlaylistOrchestrator>().save(queuePlaylist, queuePlaylist.id!!.deepOptions())
                    playlistAndItem.copy(
                        playlistId = queuePlaylist.id,
                        playlistTitle = queuePlaylist.title
                    )
                } else  playlistAndItem

                c.startActivity(
                    Intent(c, ExoActivity::class.java).apply {
                        putExtra(PLAYLIST_AND_ITEM.toString(), use.serialise())
                    })
            }

        @ExperimentalCoroutinesApi
        @JvmStatic
        val activityModule = module {
            scope(named<ExoActivity>()) {
                scoped {
                    PlayerController(
                        queueConsumer = get(),
                        modelMapper = get(),
                        coroutines = get(),
                        lifecycle = get<ExoActivity>().lifecycle.asEssentyLifecycle(),
                        log = get(),
                        mediaSessionListener = get(),
                        store = get(),
                        playSessionListener = get()
                    )
                }
                scoped {
                    PlayerStoreFactory(
                        // storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(),
                        itemLoader = get(),
                        queueConsumer = get(),
                        queueProducer = get(),
                        skip = get(),
                        coroutines = get(),
                        log = get(),
                        livePlaybackController = get(named(PlayerModule.LOCAL_PLAYER)),
                        mediaSessionManager = get(),
                        mediaSessionListener = get(),
                        mediaOrchestrator = get(),
                        playlistItemOrchestrator = get(),
                        playerSessionManager = get(),
                        playerSessionListener = get(),
                        config = PlayerContract.PlayerConfig(100f),
                        prefs = get(),
                    ).create()
                }
                scoped { ShowHideUi(get<ExoActivity>()) }
                scoped<PlayerContract.PlaylistItemLoader> { ItemLoader(get(), get()) }
                scoped { navigationRouter(false, get<ExoActivity>(), withNavHost = false) }
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
                        selectDialogCreator = SelectDialogCreator(
                            context = get<ExoActivity>()
                        )
                    )
                }
                factory<AlertDialogContract.Creator> { AlertDialogCreator(get(), get()) }
                scoped { LocalPlayerCastListener(get(), get()) }
                scoped { LinkNavigator(get(), get(), get(), get(), get(), get(), false) }
                scoped { ShareNavigationHack() }
            }
        }
    }
}
