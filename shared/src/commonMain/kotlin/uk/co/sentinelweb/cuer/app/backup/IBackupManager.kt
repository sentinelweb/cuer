package uk.co.sentinelweb.cuer.app.backup

import uk.co.sentinelweb.cuer.app.db.repository.file.AFile

interface IBackupManager {
    suspend fun makeBackupZipFile(): AFile // todo modify backup file manager and PrefBackupPresenter to use AFile
    suspend fun restoreData(data: String): Boolean
}