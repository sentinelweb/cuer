package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Application
import android.content.Intent

class CastServiceManager constructor(
    private val app: Application
) : CastServiceContract.Manager {

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

    override fun get(): CastService? = CastService.instance()

    override fun isRunning(): Boolean = CastService.instance() != null

    private fun intent() = Intent(app, CastService::class.java)

}
