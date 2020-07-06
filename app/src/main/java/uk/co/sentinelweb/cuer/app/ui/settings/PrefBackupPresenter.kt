package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.backup.BackupFileManager
import uk.co.sentinelweb.cuer.app.util.wrapper.FileWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

class PrefBackupPresenter constructor(
    private val view: PrefBackupContract.View,
    private val state: PrefBackupState,
    private val toastWrapper: ToastWrapper,
    private val backupManager: BackupFileManager,
    private val timeProvider: TimeProvider,
    private val fileWrapper: FileWrapper
) : PrefBackupContract.Presenter {

    override fun backupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .backupData()
                .also {
                    state.writeData = it
                    val device = Build.MODEL.replace(" ", "_")
                    view.promptForSaveLocation("cuer_backup-$device.json")
                    view.showProgress(false)
                }
            state.lastBackedUp = timeProvider.instant()
        }
    }

    override fun restoreFile(uriString: String) {
        view.showProgress(true)
        state.viewModelScope.launch {
            fileWrapper.readDataFromUri(uriString)
                ?.let { backupManager.restoreData(it) }
                .let { success ->
                    if (success ?: false) {
                        view.showMessage("Restore successful")
                    } else {
                        view.showMessage("Restore did NOT succceed")
                    }
                    view.showProgress(false)
                }

        }
    }

    override fun saveWriteData(uri: String) {
        state.writeData
            ?.apply { fileWrapper.writeDataToUri(uri, this) }
            ?: toastWrapper.show("No Data!!!")
    }
}