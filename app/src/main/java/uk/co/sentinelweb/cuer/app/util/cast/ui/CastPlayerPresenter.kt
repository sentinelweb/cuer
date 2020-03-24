package uk.co.sentinelweb.cuer.app.util.cast.ui

class CastPlayerPresenter(
    private val view: CastPlayerContract.View,
    private val state: CastPlayerState
) : CastPlayerContract.Presenter, CastPlayerContract.PresenterExternal {

    override fun playPressed() {

    }

    override fun pausePressed() {

    }

    override fun seekBackPressed() {

    }

    override fun seekFwdPressed() {

    }

    override fun trackBackPressed() {

    }

    override fun trackFwdPressed() {

    }

    override fun onSeekChanged(ratio: Float) {

    }

    override fun initMediaRouteButton() {
        view.initMediaRouteButton()
    }

    override fun setConnectionState(s: CastPlayerContract.ConnectionState) {
        state.connectionState = s
        view.setConnectionText(
            when (s) {
                CastPlayerContract.ConnectionState.CC_DISCONNECTED -> "-"
                CastPlayerContract.ConnectionState.CC_CONNECTING -> "*"
                CastPlayerContract.ConnectionState.CC_CONNECTED -> "="
            }
        )
    }
}