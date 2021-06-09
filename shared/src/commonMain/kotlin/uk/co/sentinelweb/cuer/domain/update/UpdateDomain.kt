package uk.co.sentinelweb.cuer.domain.update

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.domain.MediaDomain


abstract class UpdateDomain<T>

sealed class MediaUpdateDomain : UpdateDomain<MediaDomain>()

data class MediaPositionUpdateDomain(
    val id: Long,
    val duration: Long?,
    val positon: Long?,
    val dateLastPlayed: Instant?,
    val watched: Boolean
) : MediaUpdateDomain()