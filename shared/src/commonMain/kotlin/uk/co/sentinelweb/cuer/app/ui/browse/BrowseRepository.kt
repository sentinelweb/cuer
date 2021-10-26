package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseCategory
import uk.co.sentinelweb.cuer.domain.ext.makeIds

interface BrowseRepositoryJsonLoader {
    suspend fun getJson(): String
}

class BrowseRepository constructor(
    private val loader: BrowseRepositoryJsonLoader,
) {
    //   fun loadAll(): CategoryDomain = BrowseTestData.data
    private lateinit var _cache: CategoryDomain

    suspend fun loadAll(): CategoryDomain {
        if (!this::_cache.isInitialized) {
            _cache = deserialiseCategory(loader.getJson())
                .makeIds()
        }
        return _cache
    }

}