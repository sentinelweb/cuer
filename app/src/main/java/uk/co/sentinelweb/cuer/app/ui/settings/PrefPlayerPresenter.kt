package uk.co.sentinelweb.cuer.app.ui.settings

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPrefences.PLAYER_FLOAT_AUTO
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PrefPlayerPresenter constructor(
    private val view: PrefPlayerContract.View,
    private val state: PrefPlayerContract.State,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
) : PrefPlayerContract.Presenter {

    override var playerFloatAuto: Boolean
        get() = multiPrefs.getBoolean(PLAYER_FLOAT_AUTO, false)
        set(value) = multiPrefs.putBoolean(PLAYER_FLOAT_AUTO, value)

}