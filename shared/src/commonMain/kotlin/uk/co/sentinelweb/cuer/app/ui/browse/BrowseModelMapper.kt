package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.CategoryDomain.Companion.ROOT_ID

class BrowseModelMapper constructor(
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: BrowseContract.MviStore.State): Model {
        return Model(
            title = state.currentCategory.title,
            categories = state.currentCategory.subCategories
                .map { categoryModel(it) }
                .also { log.d(it.toString()) },
            isRoot = state.currentCategory.id == ROOT_ID
        )
    }

    private fun categoryModel(it: CategoryDomain): BrowseContract.View.CategoryModel = BrowseContract.View.CategoryModel(
        id = it.id,
        title = it.title,
        description = it.description,
        thumbNailUrl = it.image?.url,
        subCategories = it.subCategories.map {
            categoryModel(it)
        },
        subCount = it.subCategories.size,
        isPlaylist = it.platformId != null
    )
}
