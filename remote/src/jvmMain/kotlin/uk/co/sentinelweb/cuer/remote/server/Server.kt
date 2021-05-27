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
import java.io.File

fun main() {
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
                try {
                    call.respondBytes(
                        File(
                            File(System.getProperty("user.dir")).parent,
                            "media/data/v3-2021-05-26_13 28 23-short.json"
                        ).readBytes(),
                        ContentType.Application.Json
                    )
                } catch (e: Throwable) {
                    System.err.println("error: /playlists : " + e.message)
                    e.printStackTrace()
                    call.response.status(HttpStatusCode.InternalServerError)
                    call.respondText("Error loading file")
                }
                System.out.println("/playlists : " + call.request.uri)
            }
            static("/") {
                System.out.println("static : " + this.children.toString())
                resources("")
            }
        }
    }.start(wait = true)
}