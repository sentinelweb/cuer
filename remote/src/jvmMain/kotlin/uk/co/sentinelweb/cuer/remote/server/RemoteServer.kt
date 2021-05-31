package uk.co.sentinelweb.cuer.remote.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.util.*
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Level.ERROR
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Type.HTTP
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import uk.co.sentinelweb.cuer.remote.server.database.RemoteDatabaseAdapter
import java.io.PrintWriter
import java.io.StringWriter

@KtorExperimentalAPI
class RemoteServer constructor(
    private val database: RemoteDatabaseAdapter
) {
    val port: Int
        get() = System.getenv("PORT")?.toInt() ?: 9090

    fun fullAddress(ip: String) = "http://$ip:$port"

    private var _appEngine: ApplicationEngine? = null
    val isRunning: Boolean
        get() = _appEngine != null

    fun start() {
        _appEngine = buildServer().apply {
            start(wait = true)
        }
    }

    fun stop() {
        _appEngine?.stop(0, 0)
        _appEngine = null
    }

    private fun buildServer(): ApplicationEngine =
        embeddedServer(CIO, port) {
            install(ContentNegotiation) {
                json()
            }
            install(CORS) {
                method(HttpMethod.Get)
                method(HttpMethod.Post)
                method(HttpMethod.Delete)
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
                    System.out.println("/ : " + call.request.uri)
                }
                get("/playlists") {
                    database.getPlaylists()
                        .let { ResponseDomain(it) }
                        .apply {
                            call.respondText(serialise(), ContentType.Application.Json)
                        }
                    System.out.println("/playlists : " + call.request.uri)
                }
                get("/playlist/") {
                    call.error(HttpStatusCode.BadRequest, "No ID")
                }
                get("/playlist/{id}") {
                    (call.parameters["id"]?.toLong())
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
                    System.out.println(call.request.uri)
                }
                get("/playlistItem/") {
                    call.error(HttpStatusCode.BadRequest, "No ID")
                }
                get("/playlistItem/{id}") {
                    (call.parameters["id"]?.toLong())
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
                    System.out.println(call.request.uri)
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