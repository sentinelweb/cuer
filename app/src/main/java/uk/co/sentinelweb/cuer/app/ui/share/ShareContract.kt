package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.serialization.Transient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


interface ShareContract {

    interface Presenter {
        fun onStop()
        fun linkError(clipText: String?)
        fun scanResult(result: ScanContract.Result)
    }

    interface View {
        fun exit()
        fun gotoMain(playlistItemDomain: PlaylistItemDomain?, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commitPlaylistItems()
        fun getCommittedItems(): List<Any>?
        fun showMedia(itemDomain: PlaylistItemDomain, source: Source)
        fun showPlaylist(id: OrchestratorContract.Identifier<Long>)
    }

    interface Committer<T> {
        suspend fun commit()
        fun getEditedDomains(): List<T>?
    }

    data class Model constructor(
        val isNew: Boolean,
        val topRightButtonText: String?,
        @DrawableRes val topRightButtonIcon: Int,
        val topRightButtonAction: () -> Unit,
        val bottomRightButtonText: String?,
        @DrawableRes val bottomRightButtonIcon: Int,
        val bottomRightButtonAction: () -> Unit,
        val bottomLeftButtonText: String?,
        @DrawableRes val bottomLeftButtonIcon: Int,
        val bottomLeftButtonAction: () -> Unit,
        val topLeftButtonAction: () -> Unit,
        val topLeftButtonText: String?,
        @DrawableRes val topLeftButtonIcon: Int
    )

    //    @Serializable
    data class State(
        @Transient var model: Model? = null,
        @Transient val jobs: MutableList<Job> = mutableListOf(),
        var scanResult: ScanContract.Result? = null
    ) : ViewModel()

    class ShareDoneNavigation(private val activity: ShareActivity) : PlaylistItemEditContract.DoneNavigation {
        override fun navigateDone() {
            activity.finish()
        }
    }

    companion object {
        @JvmStatic
        val activityModule = module {
            scope(named<ShareActivity>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    SharePresenter(
                        view = get(),
                        contextProvider = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        ytContextHolder = get(),
                        mapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>()),
                        timeProvider = get()
                    )
                }
                scoped { ShareWrapper(getSource()) }
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(getSource()) }
                viewModel { State() }
                scoped {
                    ShareModelMapper(
                        ytContextHolder = get(),
                        res = get()
                    )
                }
                scoped<PlaylistItemEditContract.DoneNavigation> {
                    ShareDoneNavigation(getSource())
                }
            }
        }
    }
}