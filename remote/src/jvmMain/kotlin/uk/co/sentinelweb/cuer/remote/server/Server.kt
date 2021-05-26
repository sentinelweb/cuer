package uk.co.sentinelweb.cuer.remote.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.event.Level

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 9090
    embeddedServer(Netty, port) {
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
            level = Level.DEBUG
        }
        routing {
            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
                System.out.println("/ : " + call.request.uri)
            }
            get("/hello") {
                call.respondText("Hello, API!")
                System.out.println("/hello : " + call.request.uri)
            }
//            get("/cuer.js") {
//                call.respondBytes(
//                    this::class.java.classLoader.getResource("cuer.js")!!.readBytes(),
//                    ContentType.Application.JavaScript
//                )
//                System.out.println("/cuer.js : "+call.request.uri)
//            }
            static("/") {
                System.out.println("static : " + this.children.toString())
                resources("")
            }
        }
    }.start(wait = true)
}