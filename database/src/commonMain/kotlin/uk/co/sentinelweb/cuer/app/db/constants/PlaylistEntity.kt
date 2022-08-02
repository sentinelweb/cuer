package uk.co.sentinelweb.cuer.app.db.constants

class PlaylistEntity {
    companion object {
        const val FLAG_STARRED = 1L
        const val FLAG_ARCHIVED = 2L
        const val FLAG_DEFAULT = 4L
        const val FLAG_PLAY_ITEMS_FROM_START = 8L
    }
}