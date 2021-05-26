package uk.co.sentinelweb.cuer.app.backup.version

import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel

interface Parser {
    fun parse(data: String): BackupFileModel
}