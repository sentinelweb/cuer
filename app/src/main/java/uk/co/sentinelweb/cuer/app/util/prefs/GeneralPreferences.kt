package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    SELECTED_PLAYLIST_ID("selectedPlaylist"),
    LAST_PLAYLIST_ADDED_ID("lastAddedPlaylist"),
    PLAYING_PLAYLIST_ID("playingPlaylist")
}