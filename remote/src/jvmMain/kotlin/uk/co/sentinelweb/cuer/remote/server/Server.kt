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
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.deserialiseBackupFileModel
import uk.co.sentinelweb.cuer.domain.ext.serialise
import uk.co.sentinelweb.cuer.domain.ext.serialisePlaylists
import java.io.File

private lateinit var database: BackupFileModel

fun main() {
    val backupFile = File(
        File(System.getProperty("user.dir")).parent,
        "media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json"
    )
    database = deserialiseBackupFileModel(backupFile.readText())
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
                call.respondText(
                    database.playlists.map { it.copy(items = listOf()) }.serialisePlaylists(),
                    ContentType.Application.Json
                )
                System.out.println("/playlists : " + call.request.uri)
            }
            get("/playlist/{id}") {
                val id = call.parameters["id"]?.toLong() ?: error("Invalid playlist request")
                database.playlists.find { it.id == id }
                    ?.apply {
                        call.respondText(serialise(), ContentType.Application.Json)
                    }
                    ?: apply {
                        System.err.println("error: /playlist : $id")
                        call.response.status(HttpStatusCode.NotFound)
                        call.respondText("No playlist with ID: $id")
                    }
                System.out.println("/playlist : " + call.request.uri)
            }
            static("/") {
                System.out.println("static : " + this.children.toString())
                resources("")
            }
        }
    }.start(wait = true)
}

