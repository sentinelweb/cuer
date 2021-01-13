package uk.co.sentinelweb.cuer.app.db.backup.version.v2

import uk.co.sentinelweb.cuer.app.db.backup.version.BackupFileModel
import uk.co.sentinelweb.cuer.app.db.backup.version.Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.jsonBackupSerialzer

class V2Parser constructor() : Parser {

    override fun parse(data: String): BackupFileModel {
        val parsed = jsonBackupSerialzer.decodeFromString(BackupFileModel.serializer(), data)
        return parsed.copy(
            medias = parsed.medias.map { it.copy(channelData = it.channelData.copy(id = null)) }
        )
    }
}