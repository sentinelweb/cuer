package uk.co.sentinelweb.cuer.app.ui.cast

interface CastContract {

    interface CastDialogLauncher {
        fun launchCastDialog()

        fun hideCastDialog()
    }
}