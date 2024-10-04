package uk.co.sentinelweb.cuer.remote.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase
import uk.co.sentinelweb.cuer.core.providers.PlayerConfigProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem
import uk.co.sentinelweb.cuer.domain.ext.domainMessageJsonSerializer
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Level.ERROR
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Type.HTTP
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.interact.RemotePlayerLaunchHost
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.AVAILABLE_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.FOLDER_LIST_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_COMMAND_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_CONFIG_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_LAUNCH_VIDEO_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_LAUNCH_VIDEO_API.P_SCREEN_INDEX
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYER_STATUS_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLISTS_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLIST_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLIST_SOURCE_API
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.ext.checkNull
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage
import uk.co.sentinelweb.cuer.remote.server.message.ResponseMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerCommandMessage.*
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionHolder
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionMessageMapper
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

// todo break this up into use cases
class JvmRemoteWebServer(
    private val database: RemoteDatabaseAdapter,
    private val logWrapper: LogWrapper,
    private val connectMessageListener: RemoteServerContract.AvailableMessageHandler,
) : RemoteWebServerContract, KoinComponent {
    init {
        logWrapper.tag(this)
    }

    private val localRepository: LocalRepository by inject()
    private val playerSessionHolder: PlayerSessionHolder by inject()
    private val getFolderListUseCase: GetFolderListUseCase by inject()
    private val playerConfigProvider: PlayerConfigProvider by inject()
    private val remotePlayerLaunchHost: RemotePlayerLaunchHost by inject()
    private val playerSessionMessageMapper: PlayerSessionMessageMapper by inject()

    override val port: Int
        get() = localRepository.localNode.port

    private var _appEngine: ApplicationEngine? = null

    override val isRunning: Boolean
        get() = _appEngine != null

    override fun start(onStarted: () -> Unit) {
        buildServer().apply {
            _appEngine = this // start is a blocking call
            onStarted()
            start(wait = true)
        }
    }

    override fun stop() {
        _appEngine?.stop(0, 0)
        logWrapper.d("Stopped remote server ...")
        _appEngine = null
    }

    private fun buildServer(): ApplicationEngine =
        embeddedServer(CIO, port) {
            install(ContentNegotiation) {
                json(domainMessageJsonSerializer)
            }
            install(CORS) {
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Delete)
                anyHost()
            }
            install(Compression) {
                gzip()
            }
            install(CallLogging) {
                //level = Level.DEBUG
            }
            routing {
                get("/") {
                    call.respondText(
                        this::class.java.classLoader.getResource("index.html")!!.readText(),
                        ContentType.Text.Html
                    )
                    logWrapper.d("/ : " + call.request.uri)
                }
                get(PLAYLISTS_API.PATH) {
                    ResponseDomain(database.getPlaylists())
                        .apply {
                            call.respondText(serialise(), ContentType.Application.Json)
                        }
                    logWrapper.d("/playlists : " + call.request.uri)
                }
                get("/playlist/") {
                    call.error(HttpStatusCode.BadRequest, "No ID")
                }
                get(PLAYLIST_API.PATH) {
                    (call.parameters["id"]?.toGuidIdentifier(LOCAL)) // fixme just deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
                        ?.let { id ->
                            database.getPlaylist(id)
                                ?.let { ResponseDomain(it) }
                                ?.apply {
                                    call.respondText(serialise(), ContentType.Application.Json)
                                }
                                ?: apply {
                                    call.error(HttpStatusCode.NotFound, "No playlist with ID: $id")
                                }
                        }
                    logWrapper.d(call.request.uri)
                }
                get(PLAYLIST_SOURCE_API.PATH) {
                    (call.parameters["src"] to call.parameters["id"])
                        .takeIf { it.checkNull() != null }?.checkNull()
                        ?.let {
                            it.second.toGuidIdentifier(Source.valueOf(it.first)) // fixme just deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
                                .let { id ->
                                    database.getPlaylist(id)
                                        ?.let { ResponseDomain(it) }
                                        ?.apply {
                                            call.respondText(serialise(), ContentType.Application.Json)
                                        }
                                        ?: apply {
                                            call.error(HttpStatusCode.NotFound, "No playlist with ID: $id")
                                        }
                                }
                            logWrapper.d(call.request.uri)
                        }
                }
                get("/playlistItem/") {
                    call.error(HttpStatusCode.BadRequest, "No ID")
                }
                get("/playlistItem/{id}") {
                    (call.parameters["id"]?.toGuidIdentifier(LOCAL)) // fixme just deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
                        ?.let { id ->
                            database.getPlaylistItem(id)
                                ?.let { ResponseDomain(it) }
                                ?.apply {
                                    call.respondText(serialise(), ContentType.Application.Json)
                                }
                                ?: apply {
                                    call.error(HttpStatusCode.NotFound, "No playlist with ID: $id")
                                }
                        }
                    logWrapper.d(call.request.uri)
                }
                post(AVAILABLE_API.PATH) {
                    logWrapper.d("${AVAILABLE_API.PATH} : " + call.request.uri)
                    (try {
                        call.receive<RequestMessage>()
                    } catch (e: BadRequestException) {
                        logWrapper.e("connect: bad request", e)
                        null
                    })
                        ?.let { it.payload as AvailableMessage }
                        ?.also { connectMessageListener.messageReceived(it) }
                        ?.also { call.respond(HttpStatusCode.OK) }
                        ?: call.error(HttpStatusCode.BadRequest, "No message")
                }
                post("/checkLink") {
                    val post = call.receiveParameters()
                    //logWrapper.d("scan:" + post)
                    try {
                        post["url"]
                            ?.let { url ->
                                //logWrapper.d("scan:" + url)
                                try {
                                    database.scanUrl(url)
                                        ?.let { ResponseDomain(it) }
                                        ?.apply {
                                            call.respondText(serialise(), ContentType.Application.Json)
                                        }
                                        ?: apply {
                                            call.error(HttpStatusCode.NotFound, "Could not scan url: $url")
                                        }
                                } catch (e: Throwable) {
                                    call.error(HttpStatusCode.NotFound, null, e)
                                }
                            }
                            ?: call.error(HttpStatusCode.BadRequest, "url is required")
                    } catch (e: Throwable) {
                        call.error(HttpStatusCode.InternalServerError, null, e)
                    }
                    logWrapper.d(call.request.uri)
                }

                post("/addItem") {
                    val post = call.receiveParameters()
                    //logWrapper.d("scan:" + post)
                    try {
                        (post["item"])
                            ?.let { deserialisePlaylistItem(it) }
                            ?.let { item ->
                                database.commitPlaylistItem(item)
                                    .let { ResponseDomain(it) }
                                    .apply {
                                        call.respondText(serialise(), ContentType.Application.Json)
                                    }

                            }
                            ?: call.error(HttpStatusCode.BadRequest, "url is required")
                    } catch (e: Throwable) {
                        call.error(HttpStatusCode.InternalServerError, null, e)
                    }
                    logWrapper.d(call.request.uri)
                }
                // http://192.168.1.x:9090/player/PlayPause/true
                // todo modify arg to be a parameter not a part of the url
                get(PLAYER_COMMAND_API.PATH) {
                    try {
                        val command = call.parameters["command"]
                        val arg0 = call.parameters["arg0"]
                        logWrapper.d("${call.request.uri} $command $arg0 ${SkipFwd::class.java.simpleName}")
                        when (command) {
                            SkipFwd::class.java.simpleName -> SkipFwd
                            SkipBack::class.java.simpleName -> SkipBack
                            TrackFwd::class.java.simpleName -> TrackFwd
                            TrackBack::class.java.simpleName -> TrackBack
                            FocusWindow::class.java.simpleName -> FocusWindow
                            Stop::class.java.simpleName -> Stop
                            PlayPause::class.java.simpleName -> PlayPause(isPlaying = arg0.toBoolean())
                            SeekToFraction::class.java.simpleName -> SeekToFraction(fraction = arg0?.toFloat() ?: 0f)
                            Volume::class.java.simpleName -> Volume(volume = arg0?.toFloat() ?: 0f)
                            else -> null
                        }?.also {
                            // fixme get operation success and return the code
                            logWrapper.d("messageParsed: $it: ${playerSessionHolder.playerSession}")
                            playerSessionHolder.playerSession
                                ?.apply { controlsListener.messageRecieved(it) }
                                ?.let { playerSessionMessageMapper.map(it) }
                                ?.let { ResponseMessage(it) }
                                ?.apply { call.respondText(serialise(), ContentType.Application.Json) }
                                ?: call.error(HttpStatusCode.ServiceUnavailable, "player session not available")
                        } ?: call.error(HttpStatusCode.BadRequest, "url is required")
                    } catch (e: NumberFormatException) {
                        call.error(HttpStatusCode.BadRequest, "Number format is incorrect: ${e.message}")
                    } catch (e: Exception) {
                        call.error(HttpStatusCode.InternalServerError, e.message)
                    }
                }

                get(PLAYER_STATUS_API.PATH) {
                    try {
                        playerSessionHolder.playerSession
                            ?.let { playerSessionMessageMapper.map(it) }
                            //?.also{logWrapper.d("Mapped player status message: $it")}
                            ?.let { ResponseMessage(it) }
                            ?.apply { call.respondText(serialise(), ContentType.Application.Json) }
                            ?: call.error(HttpStatusCode.ServiceUnavailable, "player session invalid")
                    } catch (e: NumberFormatException) {
                        call.error(HttpStatusCode.BadRequest, "Number format is incorrect: ${e.message}")
                    } catch (e: Exception) {
                        call.error(HttpStatusCode.InternalServerError, e.message)
                        logWrapper.e("Exception player status", e)
                    }
                }

                get(PLAYER_CONFIG_API.PATH) {
                    try {
                        ResponseDomain(playerConfigProvider.invoke())
                            .apply {
                                call.respondText(serialise(), ContentType.Application.Json)
                            }
                    } catch (e: Exception) {
                        call.error(HttpStatusCode.InternalServerError, e.message)
                    }
                }

                post(PLAYER_LAUNCH_VIDEO_API.PATH) {
                    val postData = call.receiveText()
                    try {
                        (postData)
                            .let { deserialisePlaylistItem(it) }
//                            .let {it.copy(media = it.media.copy())}
                            .let { item ->
                                remotePlayerLaunchHost.launchVideo(
                                    item,
                                    // todo send bad request if no P_SCREEN_INDEX
                                    call.parameters[P_SCREEN_INDEX]?.toInt()
                                )
                            }
                            .also { call.respond(HttpStatusCode.OK) }
                            ?: call.error(HttpStatusCode.BadRequest, "item is required")
                    } catch (e: Throwable) {
                        call.error(HttpStatusCode.InternalServerError, null, e)
                    }
                    logWrapper.d(call.request.uri)
                }

                get(FOLDER_LIST_API.PATH) {
                    val path = call.parameters[FOLDER_LIST_API.P_PARAM]
                    logWrapper.d("Folder: $path")
                    getFolderListUseCase.getFolderList(path)
                        ?.let { ResponseDomain(it) }
                        ?.apply { call.respondText(serialise(), ContentType.Application.Json) }
                        ?: apply {
                            call.respond(HttpStatusCode.NotFound, "No folder with path: $path")
                        }
                }
                //http://192.168.1.12:9843/video-stream/torrent:::::farscape-s1:::::Farscape%20S01E03%20Back%20and%20Back%20and%20Back%20to%20the%20Future.mp4
                get("/video-stream/{filePath}") {
                    val filePath = call.parameters["filePath"]
//                        ?.substring("/video-stream".length)
                        ?.replace(":::::","/")
                    if (filePath == null) {
                        call.respond(HttpStatusCode.BadRequest, "File filePath is missing")
                        return@get
                    }
                    logWrapper.d("/video-stream/filepath: $filePath")

                    val parentPath = filePath.substring(0, filePath.lastIndexOf("/"))
                    val item = getFolderListUseCase.getFolderList(parentPath)
                        ?.children?.filter { it.platformId?.endsWith(filePath)?:false }
                    val fullPath =  getFolderListUseCase.truncatedToFullFolderPath(filePath)
                    logWrapper.d("/video-stream/filepath: $fullPath")

                    val file = File(fullPath)
                    if (!file.exists()) {
                        call.respond(HttpStatusCode.NotFound, "File not found")
                        return@get
                    }

                    val byteRange = call.request.header(HttpHeaders.Range)
                    if (byteRange != null) {
                        val range = parseRange(byteRange, file.length())
                        if (range != null) {
                            val (start, end) = range
                            call.response.header(HttpHeaders.ContentRange, "bytes $start-$end/${file.length()}")
                            call.respondBytesWriter(
                                status = HttpStatusCode.PartialContent,
                                contentType = ContentType.defaultForFile(file)
                            ) {
                                writeFilePart(file, start, end)
                            }
                        } else {
                            call.respond(HttpStatusCode.RequestedRangeNotSatisfiable)
                        }
                    } else {
                        call.respondFile(file)
                    }
                }
                static("/") {
                    resources("")
                }
            }
        }
}


suspend fun ApplicationCall.error(
    status: HttpStatusCode = HttpStatusCode.InternalServerError,
    message: String? = null,
    error: Throwable? = null
) {
    val errorString = error?.let { e ->
        StringWriter()
            .apply { e.printStackTrace(PrintWriter(this)) }
            .toString()
    }
    val messageFull = status.description + " : " + (message ?: error?.message ?: "")
    response.status(status)
    respondText(
        ResponseDomain(ErrorDomain(ERROR, HTTP, status.value, messageFull, errorString)).serialise(),
        ContentType.Application.Json
    )
}


fun parseRange(rangeHeader: String, fileLength: Long): Pair<Long, Long>? {
    val range = rangeHeader.removePrefix("bytes=").split("-")
    val start = range[0].toLongOrNull() ?: return null
    val end = range.getOrNull(1)?.toLongOrNull() ?: (fileLength - 1)
    return if (start <= end) start to end else null
}

suspend fun ByteWriteChannel.writeFilePart(file: File, start: Long, end: Long) =
    withContext(Dispatchers.IO) {
        file.inputStream().use { inputStream ->
            inputStream.skip(start)
            var remaining = end - start + 1
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (remaining > 0) {
                val read = inputStream.read(buffer, 0, minOf(buffer.size.toLong(), remaining).toInt())
                if (read == -1) break
                writeFully(buffer, 0, read)
                remaining -= read
            }
        }
    }
