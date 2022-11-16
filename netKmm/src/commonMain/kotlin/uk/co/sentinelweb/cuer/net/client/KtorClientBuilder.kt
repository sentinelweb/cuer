package uk.co.sentinelweb.cuer.net.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.net.NetModuleConfig

class KtorClientBuilder {

    fun build(config: NetModuleConfig, log: LogWrapper) = HttpClient {
        expectSuccess = false
        install(ContentNegotiation) {
            json(Json {
                classDiscriminator = "@class"
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            this.connectTimeoutMillis = config.timeoutMs
            this.socketTimeoutMillis = config.timeoutMs
        }
        if (config.debug) {
            log.tag = "KtorClient"
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        log.d(message)
                    }
                }
                level = LogLevel.HEADERS
            }
        }
    }
}