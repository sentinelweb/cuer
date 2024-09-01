package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.cast.CastContract
import uk.co.sentinelweb.cuer.app.ui.cast.CastController
import uk.co.sentinelweb.cuer.app.ui.cast.CastDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.play_control.EmptyPlayerControls
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogLauncher
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.chromecast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromecastContract
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.StatusBarColorWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


interface ShareContract {

    interface Presenter {
        fun onStop()
        fun linkError(clipText: String?)
        fun scanResult(result: ScanContract.Result)
        fun afterItemEditNavigation()
        fun isAlreadyScanned(urlOrText: String): Boolean
        fun setPlaylistParent(cat: CategoryDomain?, parentId: Identifier<GUID>)
        fun onReady(ready: Boolean)
        fun serializeState(): String?
        fun restoreState(s: String)
        fun onDestinationChange()
    }

    interface View {
        fun exit()
        fun gotoMain(plId: Identifier<GUID>, plItemId: Identifier<GUID>?, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commit(afterCommit: ShareCommitter.AfterCommit)
        fun showMedia(itemDomain: PlaylistItemDomain, playlistParentId: Identifier<GUID>?)
        fun showPlaylist(id: Identifier<GUID>, playlistParentId: Identifier<GUID>?)
        fun navigate(nav: NavigationModel)
        fun canCommit(type: ObjectTypeDomain?): Boolean
        fun hideWarning()
    }

    data class Model constructor(
        val isNew: Boolean,
        val topRight: Button,
        val bottomRight: Button,
        val bottomLeft: Button,
        val topLeft: Button,
    ) {
        data class Button constructor(
            val text: String? = null,
            @DrawableRes val icon: Int = 0,
            val action: () -> Unit = { },
            val enabled: Boolean = false,
        ) {
            val isVisible: Boolean
                get() = text != null
        }
    }

    @Serializable
    data class State(
        @Transient
        var model: Model? = null,
        var parentPlaylistId: Identifier<GUID>? = null,
        var scanResult: ScanContract.Result? = null,
        var ready: Boolean = false,
        var category: CategoryDomain? = null,
    ) : ViewModel()

    interface ShareStrings {
        val errorNoDefaultPlaylist: String
        fun errorExists(name: String): String
    }

    class AndroidShareStrings(val res: ResourceWrapper) : ShareStrings {
        override val errorNoDefaultPlaylist = res.getString(R.string.share_error_no_default_playlist)
        override fun errorExists(name: String) = res.getString(R.string.share_error_already_exists, name)
    }

    companion object {
        @JvmStatic
        val activityModule = module {
            scope(named<ShareActivity>()) {
                scoped<View> { get<ShareActivity>() }
                scoped<Presenter> {
                    SharePresenter(
                        view = get(),
                        coroutines = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        mapper = get(),
                        prefsWrapper = get(),
                        timeProvider = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        shareStrings = get(),
                        recentLocalPlaylists = get(),
                        playerConnected = get(),
                    )
                }
                scoped { AndroidShareWrapper(get<ShareActivity>()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(get<ShareActivity>(), get()) }
                viewModel { State() }
                scoped { ShareModelMapper(playerConnected = get(), res = get()) }
                scoped { navigationRouter(false, get<ShareActivity>()) }
                scoped<ShareStrings> { AndroidShareStrings(get()) }
                scoped<PlayerContract.PlayerControls> { EmptyPlayerControls() }
                scoped { AlertDialogCreator(get<ShareActivity>(), get()) }
                scoped { LinkNavigator(get(), get(), get(), get(), get(), get(), false) }
                // SHARE HACKS
                scoped<CommitHost> { get<ShareActivity>() }
                scoped<NavigationProvider> { EmptyNavigationProvider() }
                scoped<DoneNavigation> { get<ShareActivity>() }
                scoped { ShareNavigationHack() }
                // dependencies for player controls
                scoped {
                    CastController(
                        cuerCastPlayerWatcher = get(),
                        chromeCastHolder = get(),
                        chromeCastDialogWrapper = get(),
                        chromeCastWrapper = get(),
                        floatingManager = get(),
                        playerControls = get(),
                        castDialogLauncher = get(),
                        ytServiceManager = get(),
                        coroutines = get(),
                        log = get(),
                    )
                }
                scoped<CastContract.CastDialogLauncher> { CastDialogLauncher(activity = get<ShareActivity>()) }
                scoped<ChromecastContract.DialogWrapper> {
                    CastDialogWrapper(
                        activity = get<ShareActivity>(),
                        chromeCastWrapper = get()
                    )
                }
                scoped<RemotesDialogContract.Launcher> { RemotesDialogLauncher(activity = get<ShareActivity>()) }
                scoped { StatusBarColorWrapper(activity = get<ShareActivity>()) }
            }
        }
    }
}
