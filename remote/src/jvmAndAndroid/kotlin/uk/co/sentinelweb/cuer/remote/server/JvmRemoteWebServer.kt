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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import summarise
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
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLISTS_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLIST_API
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.PLAYLIST_SOURCE_API
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.ext.checkNull
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionContract.PlayerMessage.*
import uk.co.sentinelweb.cuer.remote.server.player.PlayerSessionHolder
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
                            PlayPause::class.java.simpleName -> PlayPause(isPlaying = arg0.toBoolean())
                            SeekToFraction::class.java.simpleName -> SeekToFraction(fraction = arg0?.toFloat() ?: 0f)
                            else -> null
                        }?.also {
                            // fixme get operation success and return the code
                            logWrapper.d("messageParsed: $it: ${playerSessionHolder.playerSession}")
                            playerSessionHolder.playerSession?.controlsListener?.messageRecieved(it)
                                ?.also { call.respond(HttpStatusCode.OK) }
                                ?: call.error(HttpStatusCode.BadRequest, "player session invalid")
                        } ?: call.error(HttpStatusCode.BadRequest, "url is required")
                    } catch (e: NumberFormatException) {
                        call.error(HttpStatusCode.BadRequest, "Number format is incorrect: ${e.message}")
                    } catch (e: Exception) {
                        call.error(HttpStatusCode.InternalServerError, e.message)
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
                            .also { logWrapper.d("item rx:" + it.summarise()) }
                            .also { logWrapper.d("screen index:" + call.parameters[P_SCREEN_INDEX]) }
                            .let { item ->
                                remotePlayerLaunchHost.launchVideo(
                                    item,
                                    // todo send bad request if no P_SCREEN_INDEX
                                    call.parameters[P_SCREEN_INDEX]?.toInt() ?: 0
                                )
                            }
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
                        ?.apply {
                            call.respondText(serialise(), ContentType.Application.Json)
                        }
                        ?: apply {
                            call.respond(HttpStatusCode.NotFound, "No folder with path: $path")
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