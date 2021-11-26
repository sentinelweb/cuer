package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.Order.CATEGORIES
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
        return if (state.order == CATEGORIES) {
            Model(
                title = state.currentCategory.title,
                categories = state.currentCategory.subCategories
                    .filter { it.visible }
                    .sortedBy { it.title }
                    .map { categoryModel(it) }
                    .let {
                        if (state.currentCategory.platformId?.isNotEmpty() ?: false)
                            listOf(categoryModel(state.currentCategory, true))
                                .plus(it)
                        else it
                    },
                isRoot = state.currentCategory.id == ROOT_ID,
                recent = makeRecent(state),
                order = state.order
            )
        } else {
            Model(
                categories = state.categoryLookup.values
                    .filter { it.visible }
                    .distinctBy { it.platformId }
                    .sortedBy { it.title }
                    .filter { it.platformId?.isNotEmpty() ?: false }
                    .map { categoryModel(it, true) },
                title = strings.allCatsTitle,
                isRoot = false,
                recent = makeRecent(state),
                order = state.order

            )
        }
    }

    private fun makeRecent(state: BrowseContract.MviStore.State) =
        state.recent
            //.also { log.d("Recent List:$it") }
            .takeIf { it.size > 0 }
            ?.let {
                BrowseContract.View.CategoryModel(
                    id = -1,
                    title = strings.recent,
                    description = null,
                    thumbNailUrl = null,
                    subCategories = state.recent.map { categoryModel(it) },
                    subCount = state.recent.size,
                    isPlaylist = false,
                    forceItem = true,
                )
            }


    private fun categoryModel(
        it: CategoryDomain,
        forceItem: Boolean = false
    ): BrowseContract.View.CategoryModel = BrowseContract.View.CategoryModel(
        id = it.id,
        title = it.title,
        description = it.description,
        thumbNailUrl = it.image?.url,
        subCategories = it.subCategories.map {
            categoryModel(it)
        },
        subCount = if (!forceItem) it.subCategories.size else 0,
        isPlaylist = it.platformId != null,
        forceItem = forceItem
    )
}
