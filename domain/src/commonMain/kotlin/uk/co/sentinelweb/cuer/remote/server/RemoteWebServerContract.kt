package uk.co.sentinelweb.cuer.remote.server

interface RemoteWebServerContract {
    val port: Int
    val isRunning: Boolean

    fun start(onStarted: () -> Unit)

    fun stop()

    companion object {
        const val WEB_SERVER_PORT_DEF = 9090

        object AVAILABLE_API {
            const val PATH = "/available"
        }
        object PLAYLISTS_API {
            val PATH = "/playlists"
        }

        object PLAYLIST_API {
            const val PATH = "/playlist/{id}"
        }

        object PLAYLIST_SOURCE_API {
            const val PATH = "/playlist-src/{src}/{id}"
        }

        object PLAYER_COMMAND_API {
            const val P_COMMAND = "command"
            const val P_ARG0 = "arg0"
            const val PATH = "/player/command/{$P_COMMAND}/{$P_ARG0}"
        }

        object PLAYER_CONFIG_API {
            const val PATH = "/player/config"
        }

        object PLAYER_LAUNCH_VIDEO_API {
            const val P_SCREEN_INDEX = "screenIndex"
            const val PATH = "/player/launch"
        }

        object PLAYER_STATUS_API {
            const val PATH = "/player/status"
        }

        object FOLDER_LIST_API {
            const val PATH = "/folders"
            const val P_PARAM = "p"
        }
        object VIDEO_STREAM_API {
            const val P_FILEPATH = "filePath"
            const val ROUTE = "/video-stream"
            const val PATH = "$ROUTE/{$P_FILEPATH}"
        }
    }
}
