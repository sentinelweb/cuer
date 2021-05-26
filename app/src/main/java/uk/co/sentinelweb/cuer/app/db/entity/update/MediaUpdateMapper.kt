package uk.co.sentinelweb.cuer.app.db.entity.update

import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.db.entity.MediaEntity
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.util.setFlag
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdate

class MediaUpdateMapper(val instantConverter: InstantTypeConverter) {

    fun map(updateObject: MediaPositionUpdate, flags: Long) = MediaPositionUpdateEntity(
        updateObject.id,
        updateObject.duration,
        updateObject.positon,
        instantConverter.toDb(updateObject.dateLastPlayed?.toJavaInstant()),
        setFlag(flags, MediaEntity.FLAG_WATCHED, updateObject.watched)
    )

}