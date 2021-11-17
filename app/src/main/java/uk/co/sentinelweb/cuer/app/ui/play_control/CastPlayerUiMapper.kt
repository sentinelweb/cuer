package uk.co.sentinelweb.cuer.app.ui.play_control

import uk.co.sentinelweb.cuer.core.mappers.TimeFormatter
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import kotlin.time.ExperimentalTime

class CastPlayerUiMapper constructor(
    private val dateTimeMapper: TimeFormatter,
    private val timeFormatter: TimeFormatter,
    private val timeProvider: TimeProvider,

    ) {
    fun formatTime(ms: Long): String = if (ms >= 0) {
        dateTimeMapper.formatTime(ms / 1000f)
    } else "-"

    @ExperimentalTime
    fun formatLiveTime(ms: Long): String {
        val liveTime = timeProvider.getOffsetTime(ms)
        return timeFormatter.formatTime(liveTime)
    }
}