package uk.co.sentinelweb.cuer.app.ui.main

interface MainContract {
    interface Presenter {
        fun initialise()
        fun onStart()
        fun onStop()
        fun onPlayServicesOk()
    }

    interface View {
        fun checkPlayServices()
    }
}