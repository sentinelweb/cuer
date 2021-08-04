package uk.co.sentinelweb.cuer.domain

import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE

data class CategoryDomain constructor(
    val id: Long,
    val title: String,
    val description: String? = null,
    val subCategories: List<CategoryDomain> = listOf(),
    val platform: PlatformDomain = YOUTUBE,
    val platformId: String? = null,
    val image: ImageDomain? = null,
) {
    companion object {
        val EMPTY_CATEGORY = CategoryDomain(0, "Empty")
        const val ROOT_ID = 1L
    }
}