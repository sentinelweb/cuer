package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    CURRENT_PLAYLIST_ID("currentPlaylist"),
    LAST_PLAYLIST_ADDED_ID("lastAddedPlaylist")
}