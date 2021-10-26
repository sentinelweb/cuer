package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.CategoryDomain.Companion.ROOT_ID

class BrowseModelMapper constructor(
    private val strings: BrowseContract.BrowseStrings,
    private val log: LogWrapper,
) {
    init {
        log.tag(this)
    }

    fun map(state: BrowseContract.MviStore.State): Model {
        return Model(
            title = state.currentCategory.title,
            categories = state.currentCategory.subCategories
                .map { categoryModel(it) },
            isRoot = state.currentCategory.id == ROOT_ID,
            recent = state.recent
                .also { log.d("Recent List:$it") }
                .takeIf { it.size > 0 }
                ?.let {
                    BrowseContract.View.CategoryModel(
                        id = -1,
                        title = strings.recent,
                        description = null,
                        isPlaylist = false,
                        subCount = state.recent.size,
                        thumbNailUrl = null,
                        subCategories = state.recent.map { categoryModel(it) }
                    )
                }?.also { log.d("Recent Model $it") }

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
