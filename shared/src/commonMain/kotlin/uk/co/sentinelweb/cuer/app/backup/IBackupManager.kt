package uk.co.sentinelweb.cuer.app.backup

import uk.co.sentinelweb.cuer.app.db.repository.file.AFile

interface IBackupManager {
    suspend fun makeBackupZipFile(): AFile
    suspend fun restoreData(data: String): Boolean
    suspend fun restoreDataZip(f: AFile): Boolean
}