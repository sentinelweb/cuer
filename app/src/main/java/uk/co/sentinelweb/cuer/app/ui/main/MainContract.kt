package uk.co.sentinelweb.cuer.app.ui.main

interface MainContract {
    interface Presenter {
        fun initChromecast()
    }

    interface View {
        fun initMediaRouteButton()
        fun checkPlayServices()
    }
}