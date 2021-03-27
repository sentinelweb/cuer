package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.mappers.TimeSinceFormatter

class CastPlayerUiMapper constructor(
    private val dateTimeMapper: TimeFormatter,
    private val timeSinceMapper: TimeSinceFormatter
) {
    fun formatTime(ms: Long): String = if (ms >= 0) {
        dateTimeMapper.formatTime(ms / 1000f)
    } else "-"

}