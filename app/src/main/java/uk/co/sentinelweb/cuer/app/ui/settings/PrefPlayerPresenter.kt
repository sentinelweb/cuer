package uk.co.sentinelweb.cuer.app.ui.settings

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PrefPlayerPresenter constructor(
    private val view: PrefPlayerContract.View,
    private val state: PrefPlayerContract.State,
    private val multiPrefs: MultiPlatformPreferencesWrapper,
) : PrefPlayerContract.Presenter {

    override var playerAutoFloat: Boolean
        get() = multiPrefs.playerAutoFloat
        set(value) {
            multiPrefs.playerAutoFloat = value
        }

    override var restartAfterUnlock: Boolean
        get() = multiPrefs.restartAfterUnlock
        set(value) {
            multiPrefs.restartAfterUnlock = value
        }
}
