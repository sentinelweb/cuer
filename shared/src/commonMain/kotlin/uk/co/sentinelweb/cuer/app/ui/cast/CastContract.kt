package uk.co.sentinelweb.cuer.app.ui.cast

interface CastContract {

    /* the cast dialog shows the cast status */
    interface CastDialogLauncher {
        fun launchCastDialog()

        fun hideCastDialog()
    }
}
