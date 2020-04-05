package uk.co.sentinelweb.cuer.app.ui.main

import com.google.android.gms.cast.framework.CastContext

interface MainContract {
    interface Presenter {
        fun initChromecast()
        fun setCastContext(castContext: CastContext)
        fun onStart()
        fun onStop()
    }

    interface View {
        fun checkPlayServices()
    }
}