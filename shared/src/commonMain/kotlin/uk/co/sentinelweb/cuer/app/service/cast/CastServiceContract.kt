package uk.co.sentinelweb.cuer.app.service.cast

interface CastServiceContract {

    interface Service {
        fun stopSelf()
    }

    interface Controller {
        fun initialise()
        fun handleAction(action: String?)
        fun destroy()
    }

    interface Manager {
        fun start()
        fun stop()
        fun get(): Service?
        fun isRunning(): Boolean
    }

    companion object {
        const val ACTION_PAUSE = "pause"
        const val ACTION_PLAY = "play"
        const val ACTION_SKIPF = "skipf"
        const val ACTION_SKIPB = "skipb"
        const val ACTION_TRACKF = "trackf"
        const val ACTION_TRACKB = "trackb"
        const val ACTION_DISCONNECT = "disconnect"
        const val ACTION_STAR = "star"
    }
}
