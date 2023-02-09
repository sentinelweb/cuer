package uk.co.sentinelweb.cuer.db.mapper

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.database.entity.Image
import uk.co.sentinelweb.cuer.domain.ImageDomain

class ImageMapper(private val source: Source) {

    fun map(entity: Image): ImageDomain = ImageDomain(
        entity.id.toGuidIdentifier(source),
        entity.url,
        entity.width?.toInt(),
        entity.height?.toInt()
    )

    fun map(domain: ImageDomain): Image = Image(
        domain.id?.id?.value ?: throw IllegalArgumentException("No id"),
        domain.url,
        domain.width?.toLong(),
        domain.height?.toLong()
    )
}