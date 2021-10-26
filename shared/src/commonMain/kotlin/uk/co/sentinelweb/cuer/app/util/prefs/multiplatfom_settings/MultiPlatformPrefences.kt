package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper

enum class MultiPlatformPrefences constructor(
    override val fname: String,
) : Field {
    BROWSE_CAT_TITLE("brosweNodeId")
}

interface MultiPlatformPreferencesWrapper : PrefWrapper<MultiPlatformPrefences>