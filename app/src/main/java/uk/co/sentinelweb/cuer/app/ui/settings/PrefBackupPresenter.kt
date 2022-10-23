package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.backup.BackupCheck
import uk.co.sentinelweb.cuer.app.backup.BackupFileManager
import uk.co.sentinelweb.cuer.app.backup.BackupFileManager.Companion.BACKUP_VERSION
import uk.co.sentinelweb.cuer.app.util.wrapper.ContentUriUtil
import uk.co.sentinelweb.cuer.app.util.wrapper.FileWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.ext.getFileName
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefBackupPresenter constructor(
    private val view: PrefBackupContract.View,
    private val state: PrefBackupContract.State,
    private val toastWrapper: ToastWrapper,
    private val backupManager: BackupFileManager,
    private val timeProvider: TimeProvider,
    private val fileWrapper: FileWrapper,
    private val log: LogWrapper,
    private val contentUriUtil: ContentUriUtil,
    private val backupCheck: BackupCheck,
) : PrefBackupContract.Presenter {

    override fun manualBackupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .makeBackupZipFile()
                .also {
                    state.zipFile = it
                    view.promptForSaveLocation(it.name)
                    view.showProgress(false)
                }
            state.lastBackedUp = timeProvider.instant().toJavaInstant()
        }
    }

    override fun autoBackupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .makeBackupZipFile()
                .also {
                    state.zipFile = it
                    if (backupCheck.hasAutoBackupLocation()) {
                        saveAutoBackup(backupCheck.getAutoBackupLocation()!!)
                        view.goBack()
                    } else {
                        val fileName = backupCheck.makeCurrentBackupFileName(
                            Build.MODEL,
                            BACKUP_VERSION.toString(),
                            BuildConfig.DEBUG
                        )
                        view.promptForAutoBackupLocation(fileName)
                    }
                    view.showProgress(false)
                }
        }
    }

    override fun restoreFile(uriString: String) {
        view.showProgress(true)
        state.viewModelScope.launch {
            try {
                val name = contentUriUtil.getFileName(uriString)
                    ?: throw IllegalArgumentException("invalid file chosen: url = $uriString")
                if (name.endsWith(".json")) {
                    fileWrapper.readDataFromUri(uriString)
                        ?.let { backupManager.restoreData(it) }
                        ?.let { success -> showResult(success) }
                } else if (name.endsWith(".zip")) {
                    fileWrapper.copyFileFromUri(uriString)
                        .takeIf { it.exists() }
                        ?.let { backupManager.restoreDataZip(it) }
                        ?.let { success -> showResult(success) }
                } else {
                    log.e("invalid file chosen: url = $uriString")
                    view.showMessage("Invalid file chosen:" + uriString.getFileName())
                    view.showProgress(false)
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
        state.zipFile
            ?.apply { fileWrapper.copyFileToUri(this, uri) }
            ?.apply { state.zipFile?.delete() }
            ?: toastWrapper.show("No Data!!!")
    }

    override fun gotAutoBackupLocation(uri: String) {
        backupCheck.saveAutoBackupLocation(uri)
        saveAutoBackup(uri)
    }

    private fun saveAutoBackup(uri: String) {
        (state.zipFile
            ?.apply { fileWrapper.overwriteFileToUri(this, uri) }
            ?.apply { state.zipFile?.delete() }
            ?.apply { view.showMessage("Backup succeeded ...") }
            ?.apply { backupCheck.setLastBackupNow() }
            ?: toastWrapper.show("No Data!!!"))
    }

    override fun clearAutoBackup() {
        backupCheck.clearLastBackupData()
        buildAutoSummary()
    }

    override fun buildAutoSummary() {
        if (backupCheck.hasAutoBackupLocation()) {
            view.setAutoSummary(
                backupCheck.getLastBackupTime().toString() + "\n\n" +
                        fileWrapper.getFileUriDescriptorSummary(backupCheck.getAutoBackupLocation()!!)
            )
        } else {
            view.setAutoSummary("No auto backup")
        }
    }
}