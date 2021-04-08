package uk.co.sentinelweb.cuer.domain.update

import uk.co.sentinelweb.cuer.domain.MediaDomain
import java.time.Instant

abstract class UpdateObject<T>

sealed class MediaUpdateObject : UpdateObject<MediaDomain>() {


}


class MediaPositionUpdate(
    val id: Long,
    val duration: Long?,
    val positon: Long?,
    val dateLastPlayed: Instant?,
    val watched: Boolean
) : MediaUpdateObject()