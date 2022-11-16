package uk.co.sentinelweb.cuer.app.backup

import uk.co.sentinelweb.cuer.app.db.repository.file.AFile

interface IBackupManager : IBackupJsonManager {
    suspend fun makeBackupZipFile(): AFile
    suspend fun restoreDataZip(f: AFile): Boolean
}

interface IBackupJsonManager {
    suspend fun restoreData(data: String): Boolean
}

