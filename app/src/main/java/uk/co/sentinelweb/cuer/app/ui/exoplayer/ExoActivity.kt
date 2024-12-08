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
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_AND_ITEM
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
import uk.co.sentinelweb.cuer.app.ui.ytplayer.ayt_land.AytLandActivity
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.HideStatusBarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistAndItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ExoActivity : FragmentActivity(), AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<AytLandActivity>()

    private val controller: PlayerController by inject()
    private val hideStatusBarWrapper: HideStatusBarWrapper by inject()
    private val castListener: LocalPlayerCastListener by inject()
    private val floatingService: FloatingPlayerServiceManager by inject()

    private lateinit var mviView: MviViewImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        castListener.listen()
        hideStatusBarWrapper.hide(this)
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

    inner class MviViewImpl :
        BaseMviView<Model, Event>(),
        PlayerContract.View {

        private val _model = MutableStateFlow(Initial)
        val model = _model.asStateFlow()

        override val renderer: ViewRenderer<Model> = object : ViewRenderer<Model> {
            override fun render(model: Model) {
                Log.d("ExoActivity","playState=${model.playState}, title=${model.texts.title}")
                _model.value = model
            }
        }

        private val _labelFlow = MutableSharedFlow<Label>()
        val labelFlow = _labelFlow.asSharedFlow()

        override suspend fun processLabel(label: Label) {
            when (label) {
                None -> Unit
                is Command -> label.command.let { command ->
                    _labelFlow.emit(label)
                }

                is ChannelOpen -> Unit

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
                        flags = FLAG_ACTIVITY_NEW_TASK
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
                        config = PlayerContract.PlayerConfig(1f),
                        prefs = get(),
                    ).create()
                }
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
                scoped { ShareNavigationHack() }
            }
        }
    }
}
