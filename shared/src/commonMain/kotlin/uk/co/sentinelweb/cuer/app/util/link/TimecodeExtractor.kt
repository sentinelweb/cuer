package uk.co.sentinelweb.cuer.app.util.link

import uk.co.sentinelweb.cuer.domain.TimecodeDomain

class TimecodeExtractor {

    fun extractTimecodes(text: String): List<TimecodeDomain> {
        val timecodes = REGEX
            .findAll(text)
            .map { mapUrlToTimecode(it) }
            .toList()
        return timecodes
    }

    fun mapUrlToTimecode(match: MatchResult): TimecodeDomain {
        return TimecodeDomain(
            position = match.value.timecodeToMillis(),
            title = match.value,
            extractRegion = match.range.start to match.range.endInclusive
        )
    }

    companion object {
        val REGEX =
            Regex("\\b((([0-9]?[0-9]):)?([0-5]?[0-9]):([0-5]?[0-9]))\\b")
        const val TITLE_MAX = 30
        const val TITLE_MIN = 3
    }
}

private val multipler = listOf(60*60, 60, 1).reversed()
fun String.timecodeToMillis():Long = this.split(":")
    .map { it.toLong() }
    .reversed()
    .also {println(it)}
    .foldIndexed(0L) { index, acc, digit ->
        (acc + digit * (multipler.get(index)))
            //.also { println("acc:$acc digit:$digit index:$index mul:${multipler.get(index)} = $it") }
    } * 1000
