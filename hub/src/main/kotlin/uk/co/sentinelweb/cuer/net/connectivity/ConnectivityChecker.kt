package uk.co.sentinelweb.cuer.net.connectivity

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout

class ConnectivityChecker {
    private val client = HttpClient(CIO) {
        engine {
            // Configure the connection timeout
            // fixme make configurable?
            requestTimeout = 10000
        }
    }

    suspend fun check(): Boolean = coroutineScope {
        // launch a new coroutine to handle the network request
        val networkRequest = async {
            try {
                // Send a HEAD request to google.com
                client.head("http://www.google.com")
                true
            } catch (e: IOException) {
                // Handle exceptions
                println("Network request failed: ${e.message}")
                false
            } catch (e: java.nio.channels.UnresolvedAddressException) {
                // Handle exceptions
                println("Address resolve failure: ${e.message}")
                false
            }
        }

        // Await the network request and handle timeouts
        try {
            withTimeout(1000L) { networkRequest.await() }
        } catch (e: TimeoutCancellationException) {
            println("Network request timed out")
            false
        }
    }
}
