package uk.co.sentinelweb.cuer.app.backup.version.v2

import uk.co.sentinelweb.cuer.app.backup.version.Parser
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel

class V2Parser constructor() : Parser {

    override fun parse(data: String): BackupFileModel {
        val parsed = deserialiseBackupFileModel(data)
        return parsed.copy(
            medias = parsed.medias.map { it.copy(channelData = it.channelData.copy(id = null)) }
        )
    }
}