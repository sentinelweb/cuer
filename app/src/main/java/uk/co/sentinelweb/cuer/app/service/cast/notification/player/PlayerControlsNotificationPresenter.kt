package uk.co.sentinelweb.cuer.app.service.cast.notification.player

import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerContract

class PlayerControlsNotificationPresenter constructor(
    private val view: PlayerControlsNotificationContract.View
) : PlayerControlsNotificationContract.Presenter {

    override fun initMediaRouteButton() {

    }

    override fun show() {
        view.showNotification()
    }

    override fun setConnectionState(connState: CastPlayerContract.ConnectionState) {

    }

    override fun setPlayerState(playState: CastPlayerContract.PlayerStateUi) {

    }

    override fun addListener(l: CastPlayerContract.PlayerControls.Listener) {

    }

    override fun removeListener(l: CastPlayerContract.PlayerControls.Listener) {

    }

    override fun setCurrentSecond(second: Float) {

    }

    override fun setDuration(duration: Float) {

    }

    override fun error(msg: String) {

    }

    override fun setTitle(title: String) {

    }

    override fun reset() {

    }

    override fun restoreState() {

    }
}