package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.remote.server.database.TestDatabase

fun main() {
    val database = TestDatabase.fromFile("media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json") ?: error("Couldn't load data")
    System.out.println("database loaded")
    // fixme localrepository wont inject here - need to configure koin
    JvmRemoteWebServer(database, SystemLogWrapper()).start {}
}