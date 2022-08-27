package uk.co.sentinelweb.cuer.app.db.update

import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.ext.setFlag
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain

class MediaUpdateMapper() {

    fun map(updateObject: MediaPositionUpdateDomain, flags: Long) = MediaPositionUpdateEntity(
        updateObject.id,
        updateObject.duration,
        updateObject.positon,
        updateObject.dateLastPlayed!!,
        flags.setFlag( FLAG_WATCHED, updateObject.watched)
    )

}