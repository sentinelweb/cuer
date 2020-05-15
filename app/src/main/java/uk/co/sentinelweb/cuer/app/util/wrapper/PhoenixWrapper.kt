package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import com.jakewharton.processphoenix.ProcessPhoenix

class PhoenixWrapper(private val application: Application) {

    // https://stackoverflow.com/questions/6609414/how-do-i-programmatically-restart-an-android-app
    // other methods to restart
    fun triggerRestart() {
        ProcessPhoenix.triggerRebirth(application)
    }

}