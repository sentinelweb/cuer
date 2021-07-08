package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Application
import android.content.Intent

class YoutubeCastServiceManager constructor(
    private val app: Application
) {

    fun start() {
        if (!isRunning()) {
            app.startForegroundService(intent())
        }
    }

    fun stop() {
        if (isRunning()) {
            app.stopService(intent())
        }
    }

    fun get(): YoutubeCastService? = YoutubeCastService.instance()

    fun isRunning(): Boolean = YoutubeCastService.instance() != null

    private fun intent() = Intent(app, YoutubeCastService::class.java)

}