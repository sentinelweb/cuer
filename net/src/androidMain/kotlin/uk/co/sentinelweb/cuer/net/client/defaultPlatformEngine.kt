package uk.co.sentinelweb.cuer.net.client

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual val defaultPlatformEngine: HttpClientEngine
    get() = CIO.create()