package uk.co.sentinelweb.cuer.net.pixabay.mapper

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto

internal class ImageMapper {
    internal fun map(dto: PixabayImageListDto): List<ImageDomain> = dto.hits.map {
        ImageDomain(
            url = it.webformatURL,
            width = it.webformatWidth,
            height = it.webformatHeight
        )
    }
}