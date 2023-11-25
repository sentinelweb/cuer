package uk.co.sentinelweb.cuer.remote.server

import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.remote.server.database.TestDatabase
import uk.co.sentinelweb.cuer.remote.server.message.AvailableMessage

actual fun main() {
    val database = TestDatabase.fromFile("media/data/v3-2021-05-26_13 28 23-cuer_backup-Pixel_3a.json") ?: error("Couldn't load data")
    System.out.println("database loaded")
    val logWrapper = SystemLogWrapper()
    JvmRemoteWebServer(database, logWrapper, object : RemoteServerContract.AvailableMessageHandler {
        override suspend fun messageReceived(msg: AvailableMessage) {
            logWrapper.d("message received: $msg")
        }
    }).start {}
}