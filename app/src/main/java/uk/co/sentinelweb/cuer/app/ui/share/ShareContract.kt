package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.*
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


interface ShareContract {

    interface Presenter {
        fun onStop()
        fun linkError(clipText: String?)
        fun scanResult(result: ScanContract.Result)
        fun afterItemEditNavigation()
        fun isAlreadyScanned(urlOrText: String): Boolean
        fun setPlaylistParent(cat: CategoryDomain?, parentId: Long)
        fun onReady(ready: Boolean)
        fun serializeState(): String?
        fun restoreState(s: String)
    }

    interface View {
        fun exit()
        fun gotoMain(plId: Long, plItemId: Long?, source: Source, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commit(onCommit: Committer.OnCommit)
        fun showMedia(itemDomain: PlaylistItemDomain, source: Source, playlistParentId: Long?)
        fun showPlaylist(id: OrchestratorContract.Identifier<Long>, playlistParentId: Long?)
        fun navigate(nav: NavigationModel)
    }

    interface Committer {
        suspend fun commit(onCommit: OnCommit)
        interface OnCommit {
            suspend fun onCommit(type: ObjectTypeDomain, data: List<*>)
        }
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
        var parentPlaylistId: Long? = null,
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
                scoped<View> { getSource() }
                scoped<Presenter> {
                    SharePresenter(
                        view = get(),
                        coroutines = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        ytContextHolder = get(),
                        mapper = get(),
                        prefsWrapper = get(),
                        timeProvider = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        shareStrings = get(),
                        recentLocalPlaylists = get()
                    )
                }
                scoped { ShareWrapper(getSource()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(getSource(), get()) }
                viewModel { State() }
                scoped {
                    ShareModelMapper(
                        ytContextHolder = get(),
                        res = get()
                    )
                }
                scoped<DoneNavigation> { getSource() }
                scoped { navigationRouter(false, getSource()) }
                scoped<NavigationProvider> { EmptyNavigationProvider() }
                scoped<ShareStrings> { AndroidShareStrings(get()) }
                scoped<CommitHost> { getSource() }
            }
        }
    }
}