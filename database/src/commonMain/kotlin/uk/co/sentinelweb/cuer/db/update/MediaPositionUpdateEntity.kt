package uk.co.sentinelweb.cuer.db.update

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.domain.GUID

data class MediaPositionUpdateEntity(
    val id: GUID,

    val duration: Long?,

    val positon: Long?,

    val dateLastPlayed: Instant,

    val flags: Long = 0
)