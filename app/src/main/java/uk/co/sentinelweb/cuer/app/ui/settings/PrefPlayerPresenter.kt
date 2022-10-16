package uk.co.sentinelweb.cuer.app.ui.settings

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences.Companion.PLAYER_AUTO_FLOAT_DEFAULT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences.PLAYER_AUTO_FLOAT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PrefPlayerPresenter constructor(
    private val view: PrefPlayerContract.View,
    private val state: PrefPlayerContract.State,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
) : PrefPlayerContract.Presenter {

    override var playerAutoFloat: Boolean
        get() = multiPrefs.getBoolean(PLAYER_AUTO_FLOAT, PLAYER_AUTO_FLOAT_DEFAULT)
        set(value) = multiPrefs.putBoolean(PLAYER_AUTO_FLOAT, value)

}