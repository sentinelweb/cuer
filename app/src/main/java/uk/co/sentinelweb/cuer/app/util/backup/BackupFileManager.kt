package uk.co.sentinelweb.cuer.app.util.backup

import kotlinx.coroutines.withContext
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.util.extension.deserialiseMediaList
import uk.co.sentinelweb.cuer.app.util.extension.serialiseList
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider

class BackupFileManager constructor(
    private val repository: MediaDatabaseRepository,
    private val contextProvider: CoroutineContextProvider
) {

    suspend fun backupData() = withContext(contextProvider.IO) {
        repository.loadList()
            .takeIf { it.isSuccessful }
            ?.data
            ?.serialiseList()
    }

    suspend fun restoreData(data: String) = withContext(contextProvider.IO) {
        repository.deleteAll()
            .takeIf { it.isSuccessful }
            ?.let { deserialiseMediaList(data) }
            ?.let { repository.save(it) }
            ?.isSuccessful
            ?: false
    }

}