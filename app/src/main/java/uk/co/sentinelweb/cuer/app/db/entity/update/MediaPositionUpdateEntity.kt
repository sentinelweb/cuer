package uk.co.sentinelweb.cuer.app.db.entity.update

import uk.co.sentinelweb.cuer.app.db.AppDatabase

data class MediaPositionUpdateEntity(
    val id: Long = AppDatabase.INITIAL_ID,

    val duration: Long?,

    val positon: Long?,

    val dateLastPlayed: String,

    val flags: Long = 0
)