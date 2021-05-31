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
import kotlinx.datetime.Clock
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Level.ERROR
import uk.co.sentinelweb.cuer.domain.system.ErrorDomain.Type.HTTP
import uk.co.sentinelweb.cuer.domain.system.ResponseDomain
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

data class TestDatabase constructor(
    val data: BackupFileModel,
    val items: Map<Long, PlaylistItemDomain> = data.playlists.map { it.items }.flatten().associateBy { it.id!! },
    val media: Map<Long, MediaDomain> = data.playlists.map { it.items.map { it.media } }.flatten().associateBy { it.id!! },
)

private lateinit var database: TestDatabase

fun main() {
    val backupFile = File(
        File(System.getProperty("user.dir")).parent,
        "media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json"
    )
    if (backupFile.exists()) {
        database = TestDatabase(deserialiseBackupFileModel(backupFile.readText()))
    } else {
        database = TestDatabase(
            BackupFileModel(
                playlists = listOf(
                    PlaylistDomain(
                        id = 1, title = "Test", items = listOf(
                            PlaylistItemDomain(
                                id = 1,
                                dateAdded = Clock.System.now(),
                                order = 1,
                                media = MediaDomain(
                                    id = 1,
                                    title = "marc rebillet & harry mack",
                                    url = "https://www.youtube.com/watch?v=ggLpFa6CQyU",
                                    platformId = "ggLpFa6CQyU",
                                    platform = PlatformDomain.YOUTUBE,
                                    mediaType = MediaDomain.MediaTypeDomain.VIDEO,
                                    channelData = ChannelDomain(title = "author", platformId = "xxx", platform = PlatformDomain.YOUTUBE)
                                )
                            )
                        )
                    )
                ), medias = listOf()
            )
        )
    }
    System.out.println("database loaded")
    // todo test CIO with android
    val port = System.getenv("PORT")?.toInt() ?: 9090
    embeddedServer(/*Netty*/CIO, port) {
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
                database.data.playlists.map { it.copy(items = listOf()) }
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
                        database.data.playlists.find { it.id == id }
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
                        database.items[id]
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
    }.start(wait = true)
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