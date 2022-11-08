package uk.co.sentinelweb.cuer.app.backup

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper

class BackupCheck(
    val prefs: MultiPlatformPreferencesWrapper,
    val timeProvider: TimeProvider,
    val dateTimeFormatter: DateTimeFormatter,
    val connectivityCheck: ConnectivityWrapper
) {

    fun checkToBackup(): Boolean = prefs.lastBackupInstant
        ?.let { timeProvider.instant().minus(it).inWholeSeconds > BACKUP_INTERVAL_SECS }
        ?.let { it && connectivityCheck.isConnected() && !connectivityCheck.isMetered() }
        ?: true

    fun setLastBackupNow() {
        prefs.lastBackupInstant = timeProvider.instant()
    }

    fun getLastBackupTimeFormatted() = prefs.lastBackupInstant
        ?.let { dateTimeFormatter.formatDateTime(it.toLocalDateTime(TimeZone.currentSystemDefault())) }
        ?: "Not backed up"

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
        const val BACKUP_INTERVAL_SECS = 12 * 60 * 60
        //const val BACKUP_INTERVAL_SECS = 2 * 60
    }
}