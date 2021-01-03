package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.time.Instant

interface PrefBackupContract {

    interface Presenter {
        fun backupDatabaseToJson()
        fun saveWriteData(uri: String)
        fun restoreFile(uriString: String)
        fun openRestoreFile()
    }

    interface View {
        fun promptForSaveLocation(fileName: String)
        fun showProgress(b: Boolean)
        fun showMessage(msg: String)
        fun openRestoreFile()
    }

    data class State constructor(
        var lastBackedUp: Instant? = null,
        var writeData: String? = null
    ) : ViewModel()

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefBackupFragment>()) {
                scoped<View> { getSource() }
                scoped<Presenter> {
                    PrefBackupPresenter(
                        view = get(),
                        state = get(),
                        toastWrapper = get(),
                        backupManager = get(),
                        timeProvider = get(),
                        fileWrapper = get(),
                        timeStampMapper = get(),
                        log = get()
                    )
                }
                scoped { SnackbarWrapper((getSource() as Fragment).requireActivity()) }
                viewModel { State() }
            }
        }
    }
}