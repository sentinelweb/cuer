package uk.co.sentinelweb.cuer.hub.ui.preferences

import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper

class PreferencesModelMapper(
    private val prefs: MultiPlatformPreferencesWrapper
) {
    fun map(): PreferencesModel = PreferencesModel(
        folderRoots = prefs.folderRoots
    )
}