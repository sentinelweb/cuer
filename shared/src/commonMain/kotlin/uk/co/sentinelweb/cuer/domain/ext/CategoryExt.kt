package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.CategoryDomain

fun List<CategoryDomain>.makeIds(base: Long = 1): List<CategoryDomain> = mapIndexed { i, cat ->
    val id = i + base * 100
    cat.copy(id = id, subCategories = cat.subCategories.makeIds(id))
}