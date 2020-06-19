package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.util.backup.BackupFileManager
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
            backupManager
                .backupData()
                ?.also {
                    state.writeData = it
                    view.promptForSaveLocation("cuer_backup.json")
                }
            state.lastBackedUp = timeProvider.instant()
        }
    }

    override fun saveWriteData(uri: String) {
        state.writeData
            ?.apply { fileWrapper.writeDataToUri(uri, this) }
            ?: toastWrapper.show("No Data!!!")
    }
}