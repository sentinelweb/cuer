package uk.co.sentinelweb.cuer.app.ui.main

interface MainContract {
    interface Presenter {
        fun initialise()
        fun onStart()
        fun onStop()
        fun onPlayServicesOk()
        fun onDestroy()
        fun restartYtCastContext()
    }

    interface View {
        fun checkPlayServices()
        fun isRecreating(): Boolean
        fun showMessage(msg: String)
    }

}