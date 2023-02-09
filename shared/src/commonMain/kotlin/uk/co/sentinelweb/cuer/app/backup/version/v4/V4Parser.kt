package uk.co.sentinelweb.cuer.app.backup.version.v4

import uk.co.sentinelweb.cuer.app.backup.version.Parser
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel

class V4Parser constructor() : Parser {

    override fun parse(data: String): BackupFileModel {
        val parsed = deserialiseBackupFileModel(data)
        return parsed.copy(
            medias = parsed.playlists
                .map { it.items.map { it.media } }
                .flatten()
                .distinctBy { listOf(it.platform, it.platformId) }
            // todo check this still works
            //.map { it.copy(channelData = it.channelData.copy(id = null)) }
        )
    }
}