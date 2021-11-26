package uk.co.sentinelweb.cuer.domain.ext

import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.CategoryDomain.Companion.ROOT_ID

fun CategoryDomain.makeIds(base: Long = ROOT_ID): CategoryDomain {
    return copy(id = base, subCategories = subCategories.mapIndexed { i, cat ->
        cat.makeIds(i + base * 100)
    })
}

fun CategoryDomain.buildIdLookup(): Map<Long, CategoryDomain> =
    this.subCategories.associateBy { it.id }.toMutableMap()
        .also { map -> this.subCategories.forEach { map.putAll(it.buildIdLookup()) } }

fun CategoryDomain.buildParentLookup(): Map<CategoryDomain, CategoryDomain> =
    this.subCategories.associate { it to this }.toMutableMap()
        .also { map -> this.subCategories.forEach { map.putAll(it.buildParentLookup()) } }
