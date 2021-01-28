package uk.co.sentinelweb.cuer.app

import android.app.Application

class CuerTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme)
    }
}