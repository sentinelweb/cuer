package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AndroidSnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import java.io.File
import java.time.Instant

interface PrefBackupContract {

    interface Presenter {
        fun manualBackupDatabaseToJson()
        fun saveWriteData(uri: String)
        fun restoreFile(uriString: String)
        fun openRestoreFile()
        fun autoBackupDatabaseToJson()
        fun gotAutoBackupLocation(uri: String)
        fun clearAutoBackup()
        fun buildAutoSummary()
    }

    interface View {
        fun promptForSaveLocation(fileName: String)
        fun showProgress(b: Boolean)
        fun showMessage(msg: String)
        fun openRestoreFile()
        fun promptForAutoBackupLocation(fileName: String)
        fun goBack()
        fun setAutoSummary(summary: String)
    }

    data class State constructor(
        var lastBackedUp: Instant? = null,
        var zipFile: File? = null
    ) : ViewModel()

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefBackupFragment>()) {
                scoped<View> { get<PrefBackupFragment>() }
                scoped<Presenter> {
                    PrefBackupPresenter(
                        view = get(),
                        state = get(),
                        toastWrapper = get(),
                        backupManager = get(),
                        timeProvider = get(),
                        fileWrapper = get(),
                        log = get(),
                        contentUriUtil = get(),
                        backupCheck = get()
                    )
                }
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