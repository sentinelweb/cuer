package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlatformDomain

interface ScanContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
    }

    interface View {
        var listener: Listener
        fun fromShareUrl(uriString: String)
        fun showMessage(msg: String)
        fun setModel(model: Model)
        fun setResult(result: Result)
    }

    interface Listener {
        fun scanResult(result: Result)
    }

    class State constructor(

    ) : ViewModel()

    data class Result constructor(
        val isNew: Boolean,
        val isOnPlaylist: Boolean,
        val type: ObjectTypeDomain,
        val result: Any
    )

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
                scoped<View> { getSource() }
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
                scoped<SnackbarWrapper> { AndroidSnackbarWrapper(getSource<ScanFragment>().requireActivity()) }
                viewModel { State() }
            }
        }
    }
}