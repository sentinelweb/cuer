package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Application
import android.content.Intent

class RemoteServerServiceManager constructor(
    private val app: Application
) : RemoteServerContract.Manager {

    override fun start() {
        if (!isRunning()) {
            app.startForegroundService(intent())
        }
    }

    override fun stop() {
        if (isRunning()) {
            app.stopService(intent())
        }
    }

    override fun getService(): RemoteServerContract.Service? = RemoteServerService.instance()

    override fun isRunning(): Boolean = RemoteServerService.instance()?.isServerStarted ?: false

    private fun intent() = Intent(app, RemoteServerService::class.java)

}