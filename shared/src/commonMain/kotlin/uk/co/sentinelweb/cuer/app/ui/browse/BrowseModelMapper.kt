package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseModelMapper constructor(
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: BrowseContract.MviStore.State): Model =
//        (state.currentCategory
//            ?.let { catId -> state.categories.filter { it.id == catId } }
//            ?: state.categories)
        state.categories
            .let { Model(it.map { categoryModel(it) }) }
            .also { log.d(it.toString()) }

    private fun categoryModel(it: CategoryDomain): BrowseContract.View.CategoryModel = BrowseContract.View.CategoryModel(
        id = it.id,
        title = it.title,
        description = it.description,
        thumbNailUrl = it.thumb?.url,
        subCategories = it.subCategories.map {
            categoryModel(it)
        },
        videoCount = it.subCategories.size
    )
}
