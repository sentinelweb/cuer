package uk.co.sentinelweb.cuer.app.backup

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter.BackupResult.*
import uk.co.sentinelweb.cuer.app.util.wrapper.ContentProviderFileWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import java.io.File

class AutoBackupFileExporter(
    private val backupManager: IBackupManager,
    private val contentProviderFileWrapper: ContentProviderFileWrapper,
    private val backupCheck: BackupCheck,
    private val coroutineContextProvider: CoroutineContextProvider,
    private val log: LogWrapper,
) {

    init {
        log.tag(this)
    }

    enum class BackupResult { SUCCESS, FAIL, SETUP }

    fun attemptAutoBackup(onComplete: (BackupResult) -> Unit) {
        if (backupCheck.checkToBackup()) {
            coroutineContextProvider.mainScope.launch {
                onComplete(performAutoBackup())
            }
        }
    }

    private suspend fun performAutoBackup(): BackupResult =
        if (backupCheck.hasAutoBackupLocation()) {
            backupManager
                .makeBackupZipFile()
                .let { File(it.path) }
                .let { backupFile ->
                    if (saveAutoBackup(backupFile)) SUCCESS else FAIL
                }
        } else SETUP

    private fun saveAutoBackup(file: File): Boolean = try {
        file
            .apply { contentProviderFileWrapper.overwriteFileToUri(this, backupCheck.getAutoBackupLocation()!!) }
            .apply { delete() }
            .apply { backupCheck.setLastBackupNow() }
            .let { true }
    } catch (e: java.io.FileNotFoundException) {
        // seems to happen after restart at least for dropbox
        log.e("Could not backup data", e)
        false
    } catch (e: java.io.IOException) {
        // seems to happen after restart at least for dropbox
        log.e("Could not backup data", e)
        false
    }
}
