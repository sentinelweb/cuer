package uk.co.sentinelweb.cuer.app.db.backup.version

interface Parser {
    fun parse(data: String): BackupFileModel
}