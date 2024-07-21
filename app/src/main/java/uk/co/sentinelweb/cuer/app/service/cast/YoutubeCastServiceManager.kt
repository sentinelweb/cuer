package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Application
import android.content.Intent

class YoutubeCastServiceManager constructor(
    private val app: Application
) : YoutubeCastServiceContract.Manager {

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

    override fun get(): YoutubeCastService? = YoutubeCastService.instance()

    override fun isRunning(): Boolean = YoutubeCastService.instance() != null

    private fun intent() = Intent(app, YoutubeCastService::class.java)

}