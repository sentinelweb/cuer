package uk.co.sentinelweb.cuer.app.ui.remotes

import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseNodeList

class RemotesRepositoryJsonLoader(val assetOperation: AssetOperations) {
    fun getJson(path: String): String = assetOperation.getAsString(path)
        ?: throw IllegalStateException("Couldn't load categories")
}

class RemotesRepository constructor(
    private val loader: RemotesRepositoryJsonLoader,
    private val fileName: String
) {
    private lateinit var _cache: List<NodeDomain>

    suspend fun loadAll(): List<NodeDomain> {
        if (!this::_cache.isInitialized) {
            _cache = deserialiseNodeList(loader.getJson(fileName))
        }
        return _cache
    }

}