package uk.co.sentinelweb.cuer.core.mappers

import kotlinx.datetime.LocalDateTime

actual class TimeFormatter {
    actual fun formatTime(time: LocalDateTime, format: Format): String {
        TODO("Not yet implemented")
    }

    actual fun formatTime(timeSecs: Float, format: Format): String {
        TODO("Not yet implemented")
    }

    actual fun formatNow(format: Format): String {
        TODO("Not yet implemented")
    }

    actual fun formatMillis(l: Long, format: Format): String {
        TODO("Not yet implemented")
    }

    actual fun formatFrom(time: LocalDateTime, format: Format): String {
        TODO("Not yet implemented")
    }
}

