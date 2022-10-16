package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper

enum class MultiPlatformPrefences constructor(
    override val fname: String,
) : Field {
    BROWSE_CAT_TITLE("browseNodeId"),
    BROWSE_RECENT_TITLES("browseRecent"),
    RECENT_PLAYLISTS("recentPlaylists"),
    FLOATING_PLAYER_RECT("floatingPlayerRect"),
    SHOW_VIDEO_CARDS("showVideoCards"),
    PLAYER_FLOAT_AUTO("playerFloatAuto"),
}

interface MultiPlatformPreferencesWrapper : PrefWrapper<MultiPlatformPrefences>