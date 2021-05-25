package uk.co.sentinelweb.cuer.domain

data class PlaylistStatDomain constructor(
    val playlistId: Long,
    val itemCount: Int,
    val watchedItemCount: Int
)