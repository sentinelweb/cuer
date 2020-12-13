package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter

class CastPlayerUiMapper constructor(
    private val dateTimeMapper: TimeFormatter
) {
    fun formatTime(ms: Long): String = if (ms >= 0) {
        dateTimeMapper.formatTime(ms / 1000f)
    } else "-"
}