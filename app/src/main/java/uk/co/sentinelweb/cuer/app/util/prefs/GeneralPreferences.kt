package uk.co.sentinelweb.cuer.app.util.prefs

enum class GeneralPreferences constructor(
    override val fname: String
) : Field {
    CURRENT_PLAYLIST_ID("currentPlaylist"),
    LAST_PLAYLIST_ADDED_ID("lastAddedPlaylist"),
    LAST_PLAYLIST_VIEWED_ID("lastViewedPlaylist"),
    SKIP_FWD_TIME("skipFwdTime"),
    SKIP_BACK_TIME("skipBackTime"),
    YT_LIVE_STREAM_VIDEO_ID("ytLiveStreamId"),
    YT_LIVE_STREAM_VIDEO_START_POSITION("ytLiveStreamStartPos")
}