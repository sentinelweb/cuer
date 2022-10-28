package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.app.backup.BackupCheck
import uk.co.sentinelweb.cuer.app.backup.BackupFileManager.Companion.BACKUP_VERSION
import uk.co.sentinelweb.cuer.app.backup.IBackupManager
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.util.wrapper.ContentProviderFileWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ContentUriUtil
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.ext.getFileName
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.io.File

class PrefBackupPresenter constructor(
    private val view: PrefBackupContract.View,
    private val state: PrefBackupContract.State,
    private val toastWrapper: ToastWrapper,
    private val backupManager: IBackupManager,
    private val timeProvider: TimeProvider,
    private val contentProviderFileWrapper: ContentProviderFileWrapper,
    private val log: LogWrapper,
    private val contentUriUtil: ContentUriUtil,
    private val backupCheck: BackupCheck,
) : PrefBackupContract.Presenter {
    // region manual-backup
    override fun manualBackupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .makeBackupZipFile()
                .also {
                    state.zipFile = File(it.path)
                    view.promptForSaveLocation(state.zipFile!!.name)
                    view.showProgress(false)
                }
            state.lastBackedUp = timeProvider.instant().toJavaInstant()
        }
    }

    override fun restoreFile(uriString: String) {
        view.showProgress(true)
        state.viewModelScope.launch {
            try {
                val name = contentUriUtil.getFileName(uriString)
                    ?: throw IllegalArgumentException("invalid file chosen: url = $uriString")
                if (name.endsWith(".json")) {
                    contentProviderFileWrapper.readDataFromUri(uriString)
                        ?.let { backupManager.restoreData(it) }
                        ?.let { success -> showResult(success) }
                } else if (name.endsWith(".zip")) {
                    contentProviderFileWrapper.copyFileFromUri(uriString)
                        .takeIf { it.exists() }
                        ?.let { it to backupManager.restoreDataZip(AFile(it.absolutePath)) }
                        ?.let { (file, success) -> file.delete(); success }
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


    override fun saveWriteData(uri: String) {
        state.zipFile
            ?.apply { contentProviderFileWrapper.copyFileToUri(this, uri) }
            ?.apply { state.zipFile?.delete() }
            ?: toastWrapper.show("No Data !!!")
    }
    // endregion manual-backup

    // region auto-backup
    override fun autoBackupDatabaseToJson() {
        state.viewModelScope.launch {
            view.showProgress(true)
            backupManager
                .makeBackupZipFile()
                .also {
                    state.zipFile = File(it.path)
                    if (backupCheck.hasAutoBackupLocation()) {
                        saveAutoBackup()
                            .takeIf { success -> success }
                            ?.apply { view.goBack() }
                    } else {
                        val fileName = backupCheck.makeCurrentBackupFileName(
                            Build.MODEL,
                            BACKUP_VERSION.toString(),
                            BuildConfig.DEBUG
                        )
                        view.promptForCreateAutoBackupLocation(fileName)
                    }
                    view.showProgress(false)
                }
        }
    }

    override fun restoreAutoBackupLocation(uri: String) {
        backupCheck.clearLastBackupData()
        backupCheck.saveAutoBackupLocation(uri)
        updateSummaryForAutoBackup()
        view.askToRestoreAutoBackup()
    }

    override fun onConfirmRestoreAutoBackup() {
        if (backupCheck.hasAutoBackupLocation()) {
            view.showProgress(true)
            state.viewModelScope.launch {
                try {
                    contentProviderFileWrapper.copyFileFromUri(backupCheck.getAutoBackupLocation()!!)
                        .takeIf { it.exists() }
                        ?.let { it to backupManager.restoreDataZip(AFile(it.absolutePath)) }
                        ?.let { (file, success) -> file.delete(); success }
                        ?.let { success -> showResult(success) }
//                        ?.apply { view.goBack() }
                } catch (e: Exception) {
                    log.e("Restore failed: url = ${backupCheck.getAutoBackupLocation()!!}", e)
                    showResult(false, e.message ?: e::class.java.simpleName)
                }
            }
        }
    }

    override fun onChooseAutoBackupFile() {
        view.promptForOpenAutoBackupLocation()
    }

    override fun gotAutoBackupLocation(uri: String) {
        backupCheck.saveAutoBackupLocation(uri)
        saveAutoBackup()
    }

    private fun saveAutoBackup(): Boolean = try {
        state.zipFile
            ?.apply { contentProviderFileWrapper.overwriteFileToUri(this, backupCheck.getAutoBackupLocation()!!) }
            ?.apply { state.zipFile?.delete() }
            ?.apply { view.showMessage("Backup succeeded ...") }
            ?.apply { backupCheck.setLastBackupNow() }
            ?.let { true }
            ?: let { view.showBackupError("Data was null or invalid"); false }
    } catch (e: java.io.FileNotFoundException) {
        // seems to happen after restart at least for dropbox
        log.e("Could not backup data", e)
        view.showBackupError(e.message ?: "File not found")
        false
    }

    override fun onClearAutoBackup() {
        backupCheck.clearLastBackupData()
        updateSummaryForAutoBackup()
    }

    override fun updateSummaryForAutoBackup() {
        if (backupCheck.hasAutoBackupLocation()) {
            val (valid, summary) =
                contentProviderFileWrapper.getFileUriDescriptorSummary(backupCheck.getAutoBackupLocation()!!)
            view.setAutoSummary(
                backupCheck.getLastBackupTimeFormatted() + "\n\n" + summary
            )
            view.setAutoBackupValid(valid)
        } else {
            view.setAutoSummary("No auto backup")
            view.setAutoBackupValid(false)
        }
    }
    // endregion auto-backup

    private fun CoroutineScope.showResult(success: Boolean?, message: String = "") {
        val msg = if (success ?: false) {
            "Restore successful"
        } else {
            "Restore did NOT succeed : $message"
        }
        if (isActive) {
            view.showMessage(msg)
            view.showProgress(false)
        } else {
            toastWrapper.show(msg)
        }
    }
}