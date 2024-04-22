package uk.co.sentinelweb.cuer.net.client

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual val defaultPlatformEngine: HttpClientEngine
    get() = Js.create()