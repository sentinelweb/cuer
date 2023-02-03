package uk.co.sentinelweb.cuer.app.backup.version.v3

import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel

class V3ToV4Mapper {

    fun map(v3Model: uk.co.sentinelweb.cuer.app.backup.version.v3.domain.BackupFileModel): BackupFileModel {
        return BackupFileModel(4, listOf(), listOf())
    }
}