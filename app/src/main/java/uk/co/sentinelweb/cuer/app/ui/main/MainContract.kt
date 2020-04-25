package uk.co.sentinelweb.cuer.app.ui.main

interface MainContract {
    interface Presenter {
        fun initialise()
        fun onStart()
        fun onStop()
        fun onPlayServicesOk()
        fun onDestroy()

    }

    interface View {
        fun checkPlayServices()
        fun isRecreating(): Boolean
    }
}