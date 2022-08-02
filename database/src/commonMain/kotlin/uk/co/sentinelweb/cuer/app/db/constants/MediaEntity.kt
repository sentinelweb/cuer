package uk.co.sentinelweb.cuer.app.db.constants

class MediaEntity {
    companion object {
        const val FLAG_WATCHED = 1L
        const val FLAG_STARRED = 2L
        const val FLAG_LIVE = 4L
        const val FLAG_LIVE_UPCOMING = 8L
        const val FLAG_PLAY_FROM_START = 16L
    }
}