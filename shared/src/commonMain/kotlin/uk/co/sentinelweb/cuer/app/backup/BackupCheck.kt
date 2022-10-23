package uk.co.sentinelweb.cuer.app.backup

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider

class BackupCheck(
    val prefs: MultiPlatformPreferencesWrapper,
    val timeProvider: TimeProvider
) {

    fun checkToBackup(): Boolean = prefs.lastBackupInstant
        ?.let { timeProvider.instant().minus(it).inWholeSeconds > BACKUP_INTERVAL_SECS }
        ?: true

    fun setLastBackupNow() {
        prefs.lastBackupInstant = timeProvider.instant()
    }

    fun getLastBackupTime() = prefs.lastBackupInstant

    fun clearLastBackupData() {
        prefs.removeLastBackupData
    }

    fun saveAutoBackupLocation(uri: String?) {
        prefs.lastBackupLocation = uri
    }

    fun hasAutoBackupLocation(): Boolean = prefs.lastBackupLocation != null

    fun getAutoBackupLocation() = prefs.lastBackupLocation

    /**
     * makeCurrentBackupFileName(model = Build.MODEL, version: VERSION)
     *
     * @param model the device model
     * @param version the backup file version
     */
    fun makeCurrentBackupFileName(model: String, version: String, debug: Boolean): String {
        val device = model.replace(" ", "_")
        val appName = if (debug) "cuer-debug" else "cuer"
        return "v$version-current-$appName-backup-$device.zip"
    }

    companion object {
        //const val BACKUP_INTERVAL_SECS = 24 * 60 * 60
        const val BACKUP_INTERVAL_SECS = 5 * 60
    }
}