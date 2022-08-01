package uk.co.sentinelweb.cuer.app.db

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class Greeting {
    fun greeting(): String {
        return "Hello, ${Platform().platform}!"
    }
}