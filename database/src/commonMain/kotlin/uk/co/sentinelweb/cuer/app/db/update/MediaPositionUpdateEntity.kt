package uk.co.sentinelweb.cuer.app.db.update

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.app.db.AppData

data class MediaPositionUpdateEntity(
    val id: Long = AppData.INITIAL_ID,

    val duration: Long?,

    val positon: Long?,

    val dateLastPlayed: Instant,

    val flags: Long = 0
)