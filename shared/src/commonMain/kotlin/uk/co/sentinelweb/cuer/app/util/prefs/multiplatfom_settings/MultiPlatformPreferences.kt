package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.PLAYER_AUTO_FLOAT_DEFAULT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.RESTART_AFTER_UNLOCK_DEFAULT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.PLAYER_AUTO_FLOAT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.RESTART_AFTER_UNLOCK

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
}