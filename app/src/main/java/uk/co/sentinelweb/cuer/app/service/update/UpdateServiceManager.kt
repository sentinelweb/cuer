package uk.co.sentinelweb.cuer.app.service.update

import android.app.Application
import android.content.Intent

class UpdateServiceManager constructor(
    private val app: Application
) : UpdateServiceContract.Manager {

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

    override fun getService(): UpdateServiceContract.Service? = UpdateService.instance()

    override fun isRunning(): Boolean = UpdateService.instance() != null

    private fun intent() = Intent(app, UpdateService::class.java)

}