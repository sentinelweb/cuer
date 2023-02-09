package uk.co.sentinelweb.cuer.core.mappers

import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import kotlin.math.abs

class TimeSinceFormatter(
    private val timeProvider: TimeProvider,
    private val logWrapper: LogWrapper
) {
    init {
        logWrapper.tag(this)
    }

    fun formatTimeSince(millis: Long): String {
        val differenceSeconds = (timeProvider.currentTimeMillis() - millis) / 1000L
        return if (differenceSeconds >= 0) {
            when {
                differenceSeconds < 5 -> "now"
                differenceSeconds < SEC_TO_MIN -> "${differenceSeconds}s"
                differenceSeconds < SEC_TO_HOURS -> "${differenceSeconds / SEC_TO_MIN}m"
                differenceSeconds < SEC_TO_DAYS -> "${differenceSeconds / SEC_TO_HOURS}h"
                differenceSeconds < SEC_TO_MONTHS -> "${differenceSeconds / SEC_TO_DAYS}d"
                differenceSeconds < SEC_TO_YEARS -> "${differenceSeconds / SEC_TO_MONTHS}mth"
                differenceSeconds < SEC_TO_YEARS * 20 -> "${differenceSeconds / SEC_TO_YEARS}y"
                else -> "--"
            }
        } else "!"
    }

    fun formatTimeShort(millis: Long): String {
        val isNegative = millis < 0
        val positiveSeconds = abs(millis) / 1000L
        val sign = if (isNegative) "-" else ""
        return when {
            positiveSeconds < SEC_TO_MIN -> "$sign${positiveSeconds}s"
            positiveSeconds < SEC_TO_HOURS -> "$sign${positiveSeconds / SEC_TO_MIN}m"
            positiveSeconds < SEC_TO_DAYS -> "$sign${positiveSeconds / SEC_TO_HOURS}h"
            positiveSeconds < SEC_TO_MONTHS -> "$sign${positiveSeconds / SEC_TO_DAYS}d"
            positiveSeconds < SEC_TO_YEARS -> "$sign${positiveSeconds / SEC_TO_MONTHS}mth"
            positiveSeconds < SEC_TO_YEARS * 20 -> "$sign${positiveSeconds / SEC_TO_YEARS}y"
            else -> "--"
        }
    }

    companion object {
        private val SEC_TO_MIN = 60
        private val SEC_TO_HOURS = 60 * 60
        private val SEC_TO_DAYS = 60 * 60 * 24
        private val SEC_TO_MONTHS = 60 * 60 * 24 * 30
        private val SEC_TO_YEARS = 60 * 60 * 24 * 365
    }
}