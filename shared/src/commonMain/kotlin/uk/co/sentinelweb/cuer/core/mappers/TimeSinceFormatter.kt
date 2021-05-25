package uk.co.sentinelweb.cuer.core.mappers

import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.math.abs

class TimeSinceFormatter(
    private val timeProvider: TimeProvider
) {
    fun formatTimeSince(millis: Long): String {
        val diff = (timeProvider.currentTimeMillis() - millis) / 1000L
        return if (diff >= 0) {
            when (diff) {
                in 0L..5L -> "now"
                in 5L..59L -> "${SECONDS.toSeconds(diff)}s"
                in 60L..60 * 60 -> "${SECONDS.toMinutes(diff)}m"
                in 60L * 60..60L * 60 * 24L -> "${SECONDS.toHours(diff)}h"
                in 60L * 60 * 24..60L * 60 * 24 * 365L -> "${SECONDS.toDays(diff)}d"
                in 60L * 60 * 24 * 365..60L * 60 * 24 * 365L * 20 -> "${SECONDS.toDays(diff) / 365}y"
                else -> "-"
            }
        } else "!"
    }

    fun formatTimeShort(diff: Long): String {
        val isNegative = diff < 0
        val xdiffSec = abs(diff) / 1000L
        val sign = if (isNegative) "-" else ""
        return when (xdiffSec) {
            in 0L..59L -> "$sign${SECONDS.toSeconds(xdiffSec)}s"
            in 60L..60 * 60 -> "$sign${SECONDS.toMinutes(xdiffSec)}m"
            in 60L * 60..60L * 60 * 24L -> "$sign${SECONDS.toHours(xdiffSec)}h"
            in 60L * 60 * 24..60L * 60 * 24 * 365L -> "$sign${SECONDS.toDays(xdiffSec)}d"
            in 60L * 60 * 24 * 365..60L * 60 * 24 * 365L * 20 -> "$sign${SECONDS.toDays(xdiffSec) / 365}y"
            else -> "-"
        }
    }
}