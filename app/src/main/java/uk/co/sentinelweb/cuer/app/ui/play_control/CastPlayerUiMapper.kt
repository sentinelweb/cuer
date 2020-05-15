package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.core.mappers.DateTimeMapper

class CastPlayerUiMapper constructor(
    private val dateTimeMapper: DateTimeMapper
) {
    fun formatTime(ms: Long): String {
        return dateTimeMapper.formatTime(ms)
    }
}