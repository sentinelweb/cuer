package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseCategory
import uk.co.sentinelweb.cuer.domain.ext.makeIds

class BrowseRepositoryJsonLoader(val assetOperation: AssetOperations) {
    fun getJson(path: String): String = assetOperation.getAsString(path)
        ?: throw IllegalStateException("Couldn't load categories")
}

class BrowseRepository constructor(
    private val loader: BrowseRepositoryJsonLoader,
    private val fileName: String
) {
    //   fun loadAll(): CategoryDomain = BrowseTestData.data
    private lateinit var _cache: CategoryDomain

    suspend fun loadAll(): CategoryDomain {
        if (!this::_cache.isInitialized) {
            _cache = deserialiseCategory(loader.getJson(fileName))
                .makeIds()
        }
        return _cache
    }

}