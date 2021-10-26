package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class BrowseJsonLoader(val res: ResourceWrapper) : BrowseRepositoryJsonLoader {
    override suspend fun getJson() = res.getAssetString("browse_categories.json")
        ?: throw IllegalStateException("Couldn't load categories")

}