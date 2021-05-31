package uk.co.sentinelweb.cuer.remote.server

import io.ktor.util.*
import uk.co.sentinelweb.cuer.remote.server.database.TestDatabase

@KtorExperimentalAPI
fun main() {
    val database = TestDatabase.fromFile("media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json") ?: error("Couldn't load data")
    System.out.println("database loaded")
    RemoteServer(database).start()
}