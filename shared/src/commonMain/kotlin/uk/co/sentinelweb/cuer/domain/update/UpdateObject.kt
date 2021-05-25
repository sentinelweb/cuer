package uk.co.sentinelweb.cuer.domain.update

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.domain.MediaDomain

//import java.time.Instant

abstract class UpdateObject<T>

sealed class MediaUpdateObject : UpdateObject<MediaDomain>() {

}

data class MediaPositionUpdate(
    val id: Long,
    val duration: Long?,
    val positon: Long?,
    val dateLastPlayed: Instant?,
    val watched: Boolean
) : MediaUpdateObject()