package uk.co.sentinelweb.cuer.app.service.cast

import android.app.Application
import android.content.Intent

class YoutubeCastServiceManager constructor(
    private val app:Application
) {

    fun start() {
        app.startForegroundService(intent())
    }

    fun stop() {
        app.stopService(intent())
    }

    fun get():YoutubeCastService? = YoutubeCastService.instance()

    private fun intent() = Intent(app, YoutubeCastService::class.java)

}