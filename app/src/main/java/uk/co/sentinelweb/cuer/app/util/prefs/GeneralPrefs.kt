package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPrefs constructor(
    override val fname: String
) : Field {
    SELECTED_PLAYLIST("selectedPlaylist")
}