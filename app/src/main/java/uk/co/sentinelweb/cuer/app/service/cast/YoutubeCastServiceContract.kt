package uk.co.sentinelweb.cuer.app.service.cast

interface YoutubeCastServiceContract {

    interface Service {
        fun stopSelf()
    }

    interface Controller {

        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
    }

}