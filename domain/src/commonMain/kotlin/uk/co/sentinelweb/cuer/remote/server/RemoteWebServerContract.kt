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

        object PLAYER_COMMAND_API {
            val P_COMMAND = "command"
            val P_ARG0 = "arg0"
            val PATH = "/player/command/{$P_COMMAND}/{$P_ARG0}"
        }

        object PLAYER_CONFIG_API {
            val PATH = "/player/config"
        }

        object PLAYER_LAUNCH_VIDEO_API {
            val P_SCREEN_INDEX = "screenIndex"
            val PATH = "/player/launch"
        }

        object PLAYER_STATUS_API {
            val PATH = "/player/status"
        }

        object FOLDER_LIST_API {
            val PATH = "/folders"
            val P_PARAM = "p"
        }
        object VIDEO_STREAM_API {
            val P_FILEPATH = "filePath"
            val ROUTE = "/video-stream"
            val PATH = "$ROUTE/{$P_FILEPATH}"
        }
    }
}
