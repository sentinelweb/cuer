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
    YT_LIVE_STREAM_VIDEO_ID("ytLiveStreamId"),
    YT_LIVE_STREAM_VIDEO_START_POSITION("ytLiveStreamStartPos"),
    LIVE_DURATION_DURATION("liveDuration"),
    LIVE_DURATION_TIME("liveDurationTime"),
    LIVE_DURATION_ID("liveDurationId")
}