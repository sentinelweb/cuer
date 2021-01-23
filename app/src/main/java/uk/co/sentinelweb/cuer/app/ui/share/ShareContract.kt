package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.serialization.Transient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
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
        fun gotoMain(media: PlaylistItemDomain?, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commitPlaylistItems()
        fun getPlaylistItems(): List<PlaylistItemDomain>
        fun showMedia(mediaDomain: PlaylistItemDomain)
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
                scoped { SnackbarWrapper(getSource()) }
                viewModel { State() }
                scoped {
                    ShareModelMapper(
                        ytContextHolder = get(),
                        res = get()
                    )
                }
            }
        }
    }
}