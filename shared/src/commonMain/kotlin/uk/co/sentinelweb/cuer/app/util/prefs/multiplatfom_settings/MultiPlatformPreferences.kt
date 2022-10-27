package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.PLAYER_AUTO_FLOAT_DEFAULT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.RESTART_AFTER_UNLOCK_DEFAULT

enum class MultiPlatformPreferences constructor(
    override val fname: String,
) : Field {
    BROWSE_CAT_TITLE("browseNodeId"),
    BROWSE_RECENT_TITLES("browseRecent"),
    RECENT_PLAYLISTS("recentPlaylists"),
    FLOATING_PLAYER_RECT("floatingPlayerRect"),
    SHOW_VIDEO_CARDS("showVideoCards"),
    PLAYER_AUTO_FLOAT("playerAutoFloat"),
    RESTART_AFTER_UNLOCK("restartAfterUnlock"),
    BACKUP_LAST("lastBackup"),
    BACKUP_LAST_LOCATION("lastBackupLocation"),
    ;

    companion object {
        const val PLAYER_AUTO_FLOAT_DEFAULT = true
        const val RESTART_AFTER_UNLOCK_DEFAULT = true
    }
}


interface MultiPlatformPreferencesWrapper : PrefWrapper<MultiPlatformPreferences> {
    var playerAutoFloat: Boolean
        get() = getBoolean(PLAYER_AUTO_FLOAT, PLAYER_AUTO_FLOAT_DEFAULT)
        set(value) = putBoolean(PLAYER_AUTO_FLOAT, value)

    var restartAfterUnlock: Boolean
        get() = getBoolean(RESTART_AFTER_UNLOCK, RESTART_AFTER_UNLOCK_DEFAULT)
        set(value) = putBoolean(RESTART_AFTER_UNLOCK, value)

    val removeLastBackupData: Boolean
        get() {
            remove(BACKUP_LAST)
            remove(BACKUP_LAST_LOCATION)
            return true
        }

    var lastBackupInstant: Instant?
        get() = getString(BACKUP_LAST, null)
            ?.let { Instant.parse(it) }
        set(value) = putString(BACKUP_LAST, value.toString())

    var lastBackupLocation: String?
        get() = getString(BACKUP_LAST_LOCATION, null)
        set(value) = value
            ?.let { putString(BACKUP_LAST_LOCATION, it) }
            ?: let { remove(BACKUP_LAST_LOCATION) }


}