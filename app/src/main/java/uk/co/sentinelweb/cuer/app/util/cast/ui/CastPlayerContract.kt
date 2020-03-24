package uk.co.sentinelweb.cuer.app.util.cast.ui

interface CastPlayerContract {

    interface Presenter {
        fun playPressed()
        fun pausePressed()
        fun seekBackPressed()
        fun seekFwdPressed()
        fun trackBackPressed()
        fun trackFwdPressed()
        fun onSeekChanged(ratio: Float)
    }

    interface PresenterExternal {

    }

    interface View {

    }
}