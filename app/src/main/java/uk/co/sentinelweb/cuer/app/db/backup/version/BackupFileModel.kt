package uk.co.sentinelweb.cuer.app.db.backup.version

import kotlinx.serialization.Serializable
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

@Serializable
data class BackupFileModel constructor(
    val version: Int = 1,
    val playlists: List<PlaylistDomain>,
    val medias: List<MediaDomain>
)