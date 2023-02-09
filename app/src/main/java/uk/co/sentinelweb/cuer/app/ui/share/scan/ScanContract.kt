package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.*

interface ScanContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
    }

    interface View {
        var listener: Listener
        fun fromShareUrl(uriString: String)
        fun showMessage(msg: String)
        fun showError(msg: String)
        fun setModel(model: Model)
        fun setResult(result: Result)
    }

    interface Listener {
        fun scanResult(result: Result)
    }

    class State constructor(

    ) : ViewModel()

    @Serializable
    data class Result constructor(
        val url: String,
        val isNew: Boolean,
        val isOnPlaylist: Boolean,
        val type: ObjectTypeDomain,
        val result: Domain
    ) {
        val platform: Pair<PlatformDomain, String> = when (result) {
            is MediaDomain -> result.platform to result.platformId
            is PlaylistDomain -> result.platform!! to result.platformId!!
            is PlaylistItemDomain -> result.media.platform to result.media.platformId
            else -> throw IllegalStateException("unknown result")
        }
    }

    data class Model constructor(
        val url: String,
        val type: ObjectTypeDomain,
        val text: String,
        val platformDomain: PlatformDomain,
        val platformId: String,
        @DrawableRes val resultIcon: Int,
        val isLoading: Boolean = false
    )

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<ScanFragment>()) {
                scoped<View> { get<ScanFragment>() }
                scoped<Presenter> {
                    ScanPresenter(
                        view = get(),
                        state = get(),
                        playlistItemOrchestrator = get(),
                        playlistOrchestrator = get(),
                        mediaOrchestrator = get(),
                        linkScanner = get(),
                        modelMapper = get(),
                        log = get()
                    )
                }
                scoped { ScanMapper() }
                scoped<SnackbarWrapper> {
                    AndroidSnackbarWrapper(
                        this.getFragmentActivity(),
                        get()
                    )
                }
                viewModel { State() }
            }
        }
    }
}