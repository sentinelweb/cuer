package uk.co.sentinelweb.cuer.net.pixabay.mapper

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto

internal class PixabayImageMapper {
    internal fun map(dto: PixabayImageListDto): List<ImageDomain> = dto.hits.map {
        ImageDomain(
            id = null,
            url = it.webformatURL,
            width = it.webformatWidth,
            height = it.webformatHeight
        )
    }
}