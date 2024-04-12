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
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toGuidIdentifier
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem
import uk.co.sentinelweb.cuer.domain.ext.domainMessageJsonSerializer
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Level.ERROR
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Type.HTTP
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.server.RemoteWebServerContract.Companion.AVAILABLE_API
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import uk.co.sentinelweb.cuer.remote.server.ext.checkNull
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage
import uk.co.sentinelweb.cuer.remote.server.message.RequestMessage
import java.io.PrintWriter
import java.io.StringWriter

class JvmRemoteWebServer constructor(
    private val database: RemoteDatabaseAdapter,
    private val logWrapper: LogWrapper,
    private val connectMessageListener: RemoteServerContract.AvailableMessageHandler
) : RemoteWebServerContract, KoinComponent {
    init {
        logWrapper.tag(this)
    }

    private val localRepository: LocalRepository by inject()

    override val port: Int
        get() = localRepository.getLocalNode().port

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
                get("/playlists") {
                    database.getPlaylists()
                        .let { ResponseDomain(it) }
                        .apply {
                            call.respondText(serialise(), ContentType.Application.Json)
                        }
                    logWrapper.d("/playlists : " + call.request.uri)
                }
                get("/playlist/") {
                    call.error(HttpStatusCode.BadRequest, "No ID")
                }
                get("/playlist/{id}") {
                    (call.parameters["id"]?.toGuidIdentifier(LOCAL)) // fixme jsut deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
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
                get("/playlist-src/{src}/{id}") {
                    (call.parameters["src"] to call.parameters["id"])
                        .takeIf { it.checkNull() != null }?.checkNull()
                        ?.let {
                            it.second.toGuidIdentifier(Source.valueOf(it.first)) // fixme jsut deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
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
                    (call.parameters["id"]?.toGuidIdentifier(LOCAL)) // fixme jsut deserialise whole id and replace LOCAL_NETWORK -> LOCAL?
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