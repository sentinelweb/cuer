package uk.co.sentinelweb.cuer.app.service.update

import android.app.Application
import android.content.Intent

class UpdateServiceManager constructor(
    private val app: Application
) : UpdateServiceContract.Manager {

    override fun start() {
        app.startForegroundService(intent())
    }

    override fun stop() {
        app.stopService(intent())
    }

    private fun intent() = Intent(app, UpdateService::class.java)

}