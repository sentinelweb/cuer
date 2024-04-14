package uk.co.sentinelweb.cuer.net.client

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual val defaultPlatformEngine: HttpClientEngine
    get() = Darwin.create()
