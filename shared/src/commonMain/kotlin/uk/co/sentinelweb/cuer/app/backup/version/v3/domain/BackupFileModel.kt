package uk.co.sentinelweb.cuer.app.backup.version.v3.domain

import kotlinx.serialization.Serializable

@Serializable
data class BackupFileModel constructor(
    val version: Int = 3,
    val playlists: List<PlaylistDomain>,
    val medias: List<MediaDomain>
)