package uk.co.sentinelweb.cuer.app.db.entity.update

import uk.co.sentinelweb.cuer.app.db.AppData

data class MediaPositionUpdateEntity(
    val id: Long = AppData.INITIAL_ID,

    val duration: Long?,

    val positon: Long?,

    val dateLastPlayed: String,

    val flags: Long = 0
)