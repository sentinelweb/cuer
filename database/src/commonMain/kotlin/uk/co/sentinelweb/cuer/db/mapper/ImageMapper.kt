package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.domain.ImageDomain

class ImageMapper {

    fun map(entity: Image): ImageDomain = ImageDomain(
        entity.id,
        entity.url,
        entity.width?.toInt(),
        entity.height?.toInt()
    )

    fun map(domain: ImageDomain): Image = Image(
        domain.id ?: 0,
        domain.url,
        domain.width?.toLong(),
        domain.height?.toLong()
    )
}