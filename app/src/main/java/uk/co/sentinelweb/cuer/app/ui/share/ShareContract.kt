package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain


interface ShareContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
        fun onStop()
        fun linkError(clipText: String?)
    }

    interface View {
        fun exit()
        fun gotoMain(media: PlaylistItemDomain?, play: Boolean = false)
        fun setData(model: Model)
        fun error(msg: String)
        fun warning(msg: String)
        suspend fun commitPlaylistItems()
        fun getPlaylistItems(): List<PlaylistItemDomain>
    }

    data class Model constructor(
        val isNewVideo: Boolean,
        val topRightButtonText: String?,
        @DrawableRes val topRightButtonIcon: Int,
        val topRightButtonAction: () -> Unit,
        val bottomRightButtonText: String?,
        @DrawableRes val bottomRightButtonIcon: Int,
        val bottomRightButtonAction: () -> Unit,
        val bottomLeftButtonText: String?,
        @DrawableRes val bottomLeftButtonIcon: Int,
        val topLeftButtonAction: () -> Unit,
        val topLeftButtonText: String?,
        @DrawableRes val topLeftButtonIcon: Int,
        val bottomLeftButtonAction: () -> Unit,
        val media: MediaDomain?
    )


    @Serializable
    data class State constructor(
        var media: MediaDomain? = null,
        var playlistItems: List<PlaylistItemDomain>? = null,
        @Transient var model: Model? = null,
        @Transient val jobs: MutableList<Job> = mutableListOf()
    ) : ViewModel()

    companion object {
        @JvmStatic
        val activityModule = module {
            scope(named<ShareActivity>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    SharePresenter(
                        view = get(),
                        repository = get(),
                        playlistRepository = get(),
                        linkScanner = get(),
                        contextProvider = get(),
                        ytInteractor = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        ytContextHolder = get(),
                        mapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>())
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