package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseRepository {
    fun loadAll(): CategoryDomain = BrowseTestData.data
}