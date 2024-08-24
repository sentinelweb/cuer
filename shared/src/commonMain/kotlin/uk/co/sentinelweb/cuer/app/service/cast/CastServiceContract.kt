package uk.co.sentinelweb.cuer.app.service.cast

interface CastServiceContract {

    interface Service {
        fun stopSelf()
    }

    interface Controller {
        fun initialise()
        fun handleAction(action: String?, extras: Map<String, Any>?)
        fun destroy()
    }

    interface Manager {
        fun start()
        fun stop()
        fun get(): Service?
        fun isRunning(): Boolean
    }

    companion object {
        const val ACTION_PAUSE = "cuer:pause"
        const val ACTION_PLAY = "cuer:play"
        const val ACTION_SKIPF = "cuer:skipf"
        const val ACTION_SKIPB = "cuer:skipb"
        const val ACTION_TRACKF = "cuer:trackf"
        const val ACTION_TRACKB = "cuer:trackb"
        const val ACTION_DISCONNECT = "cuer:disconnect"
        const val ACTION_STAR = "cuer:star"
        const val ACTION_VOL_UP = "cuer:volUp"
        const val ACTION_VOL_DOWN = "cuer:volDown"
        const val ACTION_VOL_MUTE = "cuer:volMute"
        const val ACTION_STOP = "cuer:stop"
        const val ACTION_NONE = "cuer:none"
        const val ACTION_DELETE = "cuer:delete"
    }
}
