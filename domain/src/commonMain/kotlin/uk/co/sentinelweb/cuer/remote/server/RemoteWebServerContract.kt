package uk.co.sentinelweb.cuer.remote.server

interface RemoteWebServerContract {
    val port: Int
    val isRunning: Boolean

    fun start(onStarted: () -> Unit)

    fun stop()

    companion object {
        const val WEB_SERVER_PORT_DEF = 9090

        object AVAILABLE_API {
            val PATH = "/available"
        }
        object PLAYLISTS_API {
            val PATH = "/playlists"
        }

        object PLAYLIST_API {
            val PATH = "/playlist/{id}"
        }

        object PLAYLIST_SOURCE_API {
            val PATH = "/playlist-src/{src}/{id}"
        }
    }
}