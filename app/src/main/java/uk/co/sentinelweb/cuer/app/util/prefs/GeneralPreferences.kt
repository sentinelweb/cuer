package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    CURRENT_PLAYLIST("currentPlaylist"),
    LAST_PLAYLIST_CREATED("lastPlaylistCreated"),
    LAST_PLAYLIST_VIEWED("lastViewedPlaylist"),
    LAST_PLAYLIST_ADDED_TO("lastPlaylistAddedTo"),
    SKIP_FWD_TIME("skipFwdTime"),
    SKIP_BACK_TIME("skipBackTime"),
    LIVE_DURATION_DURATION("liveDuration"),
    LIVE_DURATION_TIME("liveDurationTime"),
    LIVE_DURATION_ID("liveDurationId"),
    LAST_LOCAL_SEARCH("lastLocalSearch"),
    LAST_REMOTE_SEARCH("lastRemoteSearch"),
    LAST_SEARCH_TYPE("lastSearchType")
}