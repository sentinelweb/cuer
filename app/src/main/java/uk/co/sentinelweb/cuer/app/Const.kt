package uk.co.sentinelweb.cuer.app

import uk.co.sentinelweb.cuer.domain.PlaylistDomain

object Const {
    val EXTRA_PLAY_NOW: String = "PLAY_NOW"
    val EMPTY_PLAYLIST = PlaylistDomain(items = listOf())
    val EXTRA_YTID = "YTID"
    val EXTRA_MEDIA = "MEDIA"
}