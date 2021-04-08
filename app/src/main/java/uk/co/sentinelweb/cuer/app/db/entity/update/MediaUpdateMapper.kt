package uk.co.sentinelweb.cuer.app.db.entity.update

import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.util.setFlag
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdate

class MediaUpdateMapper {

    fun map(updateObject: MediaPositionUpdate, flags: Long) = MediaPositionUpdateEntity(
        updateObject.id,
        updateObject.duration,
        updateObject.positon,
        updateObject.dateLastPlayed,
        setFlag(flags, MediaEntity.FLAG_WATCHED, updateObject.watched)
    )

}