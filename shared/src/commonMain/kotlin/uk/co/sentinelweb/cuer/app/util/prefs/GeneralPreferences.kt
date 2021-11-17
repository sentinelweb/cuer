package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    DB_INITIALISED("dbInitialised"),
    CURRENT_PLAYLIST("currentPlaylist"),
    LAST_PLAYLIST_CREATED("lastPlaylistCreated"),
    LAST_PLAYLIST_VIEWED("lastViewedPlaylist"),
    LAST_PLAYLIST_ADDED_TO("lastPlaylistAddedTo"),
    SKIP_FWD_TIME("skipFwdTime"),
    SKIP_BACK_TIME("skipBackTime"),
    LIVE_DURATION("liveDuration"),
    LIVE_DURATION_TIME("liveDurationTime"),
    LIVE_DURATION_ID("liveDurationId"),
    LOCAL_DURATION("localDuration"),
    LOCAL_DURATION_TIME("localDurationTime"),
    LOCAL_DURATION_ID("localDurationId"),
    LAST_LOCAL_SEARCH("lastLocalSearch"),
    LAST_REMOTE_SEARCH("lastRemoteSearch"),
    LAST_SEARCH_TYPE("lastSearchType"),
    PINNED_PLAYLIST("pinnedPlaylist"),
    LAST_BOTTOM_TAB("lastBottomNavTab"),
}