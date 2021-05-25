package uk.co.sentinelweb.cuer.app.db.backup.version.v2

import uk.co.sentinelweb.cuer.app.db.backup.version.Parser
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel

class V3Parser constructor() : Parser {

    override fun parse(data: String): BackupFileModel {
        //val parsed = domainJsonSerializer.decodeFromString(BackupFileModel.serializer(), data)
        val parsed = deserialiseBackupFileModel(data)
        return parsed.copy(
            medias = parsed.playlists
                .map { it.items.map { it.media } }
                .flatten()
                .distinctBy { it.platformId }
                .map { it.copy(channelData = it.channelData.copy(id = null)) }
        )
    }
}