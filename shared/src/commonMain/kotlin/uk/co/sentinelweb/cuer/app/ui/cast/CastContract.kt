package uk.co.sentinelweb.cuer.app.ui.cast

interface CastContract {

    /* the cast dialog shows the cast status */
    interface DialogLauncher {
        fun launchCastDialog()

        fun hideCastDialog()
    }
}
