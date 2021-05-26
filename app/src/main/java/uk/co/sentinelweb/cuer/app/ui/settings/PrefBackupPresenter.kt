package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.db.backup.BackupFileManager
import uk.co.sentinelweb.cuer.app.db.backup.BackupFileManager.Companion.VERSION
import uk.co.sentinelweb.cuer.app.util.wrapper.FileWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefBackupPresenter constructor(
    private val view: PrefBackupContract.View,
    private val state: PrefBackupContract.State,
    private val toastWrapper: ToastWrapper,
    private val backupManager: BackupFileManager,
    private val timeProvider: TimeProvider,
    private val fileWrapper: FileWrapper,
    private val timeStampMapper: TimeStampMapper,
    private val log: LogWrapper
) : PrefBackupContract.Presenter {

    override fun backupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .backupData()
                .also {
                    state.writeData = it
                    val device = Build.MODEL.replace(" ", "_")
                    view.promptForSaveLocation(
                        "v$VERSION-${
                            timeStampMapper.mapDateTimeSimple(
                                timeProvider.localDateTime().toJavaLocalDateTime()
                            )
                        }-cuer_backup-$device.json"
                    )
                    view.showProgress(false)
                }
            state.lastBackedUp = timeProvider.instant().toJavaInstant()
        }
    }

    override fun restoreFile(uriString: String) {
        view.showProgress(true)
        state.viewModelScope.launch {
            try {
                fileWrapper.readDataFromUri(uriString)
                    ?.let { backupManager.restoreData(it) }
                    .let { success ->
                        showResult(success)
                    }
            } catch (e: Exception) {
                log.e("Restore failed: url = $uriString", e)
                showResult(false, e.message ?: e::class.java.simpleName)
            }
        }
    }

    override fun openRestoreFile() {
        view.openRestoreFile()
    }

    private fun CoroutineScope.showResult(success: Boolean?, message: String = "") {
        val msg = if (success ?: false) {
            "Restore successful"
        } else {
            "Restore did NOT succceed : $message"
        }
        if (isActive) {
            view.showMessage(msg)
            view.showProgress(false)
        } else {
            toastWrapper.show(msg)
        }
    }

    override fun saveWriteData(uri: String) {
        state.writeData
            ?.apply { fileWrapper.writeDataToUri(uri, this) }
            ?: toastWrapper.show("No Data!!!")
    }
}