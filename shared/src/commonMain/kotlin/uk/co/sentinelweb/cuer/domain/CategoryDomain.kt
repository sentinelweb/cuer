package uk.co.sentinelweb.cuer.domain

data class CategoryDomain constructor(
    val id: Long,
    val title: String,
    val description: String? = null,
    val subCategories: List<CategoryDomain> = listOf(),
    val media: PlaylistDomain? = null,
    val image: ImageDomain? = null,
    val thumb: ImageDomain? = null,
)