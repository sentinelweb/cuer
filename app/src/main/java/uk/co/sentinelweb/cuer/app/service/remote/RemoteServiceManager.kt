package uk.co.sentinelweb.cuer.app.service.remote

import android.app.Application
import android.content.Intent

class RemoteServiceManager constructor(
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

    fun get(): RemoteService? = RemoteService.instance()

    fun isRunning(): Boolean = RemoteService.instance() != null

    private fun intent() = Intent(app, RemoteService::class.java)

}