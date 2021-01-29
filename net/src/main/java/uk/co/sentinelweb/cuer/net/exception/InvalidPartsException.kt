package uk.co.sentinelweb.cuer.net.exception

import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart

class InvalidPartsException(parts: List<YoutubePart>) : Exception("Request must include include parts $parts") {
    constructor(part: YoutubePart) : this(listOf(part))
}