package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper

enum class MultiPlatformPrefences constructor(
    override val fname: String,
) : Field {
    BROWSE_CAT_TITLE("brosweNodeId"),
    BROWSE_RECENT_TITLES("brosweRecent")
}

interface MultiPlatformPreferencesWrapper : PrefWrapper<MultiPlatformPrefences>