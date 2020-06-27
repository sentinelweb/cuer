package uk.co.sentinelweb.cuer.app.db.mapper

import uk.co.sentinelweb.cuer.app.db.entity.Image
import uk.co.sentinelweb.cuer.domain.ImageDomain

class ImageMapper {
    fun mapImage(thumbNail: ImageDomain?) =
        thumbNail?.let {
            Image(
                it.url,
                it.width,
                it.height
            )
        }

    fun mapImage(thumbNail: Image?) =
        thumbNail?.let {
            ImageDomain(
                it.url,
                it.width,
                it.height
            )
        }
}