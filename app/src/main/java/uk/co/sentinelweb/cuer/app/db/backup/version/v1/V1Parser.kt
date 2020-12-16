package uk.co.sentinelweb.cuer.app.db.backup.version.v1

import uk.co.sentinelweb.cuer.app.db.backup.version.BackupFileModel
import uk.co.sentinelweb.cuer.app.db.backup.version.Parser
import uk.co.sentinelweb.cuer.app.db.backup.version.v1.v1Serializer.deserialiseMediaList

class V1Parser constructor(private val mapper: V1Mapper) : Parser {

    override fun parse(data: String) = BackupFileModel(
        version = 1,
        medias = deserialiseMediaList(data).map { mapper.map(it) },
        playlists = listOf()
    )
}