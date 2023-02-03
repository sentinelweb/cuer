package uk.co.sentinelweb.cuer.app.backup.version.v3

import uk.co.sentinelweb.cuer.app.backup.version.Parser
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.deserialiseV3BackupFileModel
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel

class V3Parser(val v4Mapper: V3ToV4Mapper) : Parser {

    override fun parse(data: String): BackupFileModel = parseV3(data).let { v4Mapper.map(it) }

    // VisibleForTesting
    fun parseV3(data: String): uk.co.sentinelweb.cuer.app.backup.version.v3.domain.BackupFileModel {
        val parsed = deserialiseV3BackupFileModel(data)
        return parsed.copy(
            medias = parsed.playlists
                .map { it.items.map { it.media } }
                .flatten()
                .distinctBy { it.platformId }
                .map { it.copy(channelData = it.channelData.copy(id = null)) }
        )
    }
}
