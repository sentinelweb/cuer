package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.serialization.Transient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


interface ShareContract {

    interface Presenter {
        fun onStop()
        fun linkError(clipText: String?)
        fun scanResult(result: ScanContract.Result)
        fun afterItemEditNavigation()
        fun isAlreadyScanned(urlOrText: String): Boolean
    }

    interface View {
        fun exit()
        fun gotoMain(plId: Long, plItemId: Long?, source: Source, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commit(onCommit: Committer.OnCommit)
        fun showMedia(itemDomain: PlaylistItemDomain, source: Source)
        fun showPlaylist(id: OrchestratorContract.Identifier<Long>)
        fun navigate(nav: NavigationModel)
    }

    interface Committer {
        suspend fun commit(onCommit: OnCommit)
        interface OnCommit {
            suspend fun onCommit(type: ObjectTypeDomain, data: List<*>)
        }
    }

    data class Model constructor(// todo make button type
        val isNew: Boolean,
        val topRight: Button,
        val bottomRight: Button,
        val bottomLeft: Button,
        val topLeft: Button
    ) {
        data class Button constructor(
            val text: String? = null,
            @DrawableRes val icon: Int = 0,
            val action: () -> Unit = { }
        ) {
            val isVisible: Boolean
                get() = text != null
        }
    }

    //    @Serializable
    data class State(
        @Transient var model: Model? = null,
        var scanResult: ScanContract.Result? = null
    ) : ViewModel()

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
                        prefsWrapper = get(named<GeneralPreferences>()),
                        timeProvider = get(),
                        playlistItemOrchestrator = get()
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
                scoped<PlaylistItemEditContract.DoneNavigation> { getSource() }
                scoped {
                    NavigationMapper(
                        activity = getSource(),
                        toastWrapper = get(),
                        ytJavaApi = get(),
                        navController = get(),
                        log = get()
                    )
                }
                scoped<NavController> {
                    (getSource<AppCompatActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                        .navController
                }
                scoped { YoutubeJavaApiWrapper(getSource()) }
            }
        }
    }
}