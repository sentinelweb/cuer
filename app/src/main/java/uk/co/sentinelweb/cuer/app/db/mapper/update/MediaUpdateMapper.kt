package uk.co.sentinelweb.cuer.app.db.mapper.update

import kotlinx.datetime.toJavaInstant
import uk.co.sentinelweb.cuer.app.db.entity.update.MediaPositionUpdateEntity
import uk.co.sentinelweb.cuer.app.db.typeconverter.InstantTypeConverter
import uk.co.sentinelweb.cuer.app.db.util.setFlag
import uk.co.sentinelweb.cuer.domain.MediaDomain.Companion.FLAG_WATCHED
import uk.co.sentinelweb.cuer.domain.update.MediaPositionUpdateDomain

class MediaUpdateMapper(val instantConverter: InstantTypeConverter) {

    fun map(updateObject: MediaPositionUpdateDomain, flags: Long) =
        MediaPositionUpdateEntity(
            updateObject.id,
            updateObject.duration,
            updateObject.positon,
            instantConverter.toDb(updateObject.dateLastPlayed?.toJavaInstant()),
            setFlag(flags, FLAG_WATCHED, updateObject.watched)
        )

}