package uk.co.sentinelweb.cuer.app.ui.browse

import uk.co.sentinelweb.cuer.domain.CategoryDomain

class BrowseModelMapper {
    fun map(state: BrowseContract.MviStore.State): BrowseContract.View.Model {
        return state.categories.find { it.id == state.currentCategory }!!// todo error
            .let {
                BrowseContract.View.Model(listOf(categoryModel(it)))
            }
    }

    private fun categoryModel(it: CategoryDomain) = BrowseContract.View.CategoryModel(
        id = it.id,
        title = it.title,
        description = it.description,
        subCategories = it.subCategories.map {
            subcategoryModel(it)
        }
    )

    //todo weird should be able to recurese with fun categoryModel
    private fun subcategoryModel(it: CategoryDomain) = BrowseContract.View.CategoryModel(
        id = it.id,
        title = it.title,
        description = it.description,
        subCategories = listOf()
    )
}
