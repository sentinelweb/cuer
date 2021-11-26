package uk.co.sentinelweb.cuer.app.util.wrapper

import android.app.Application
import com.facebook.stetho.Stetho
import uk.co.sentinelweb.cuer.app.CuerApp

class StethoWrapper (val app: Application) {
    fun init() {
        Stetho.initializeWithDefaults(app)
    }
}