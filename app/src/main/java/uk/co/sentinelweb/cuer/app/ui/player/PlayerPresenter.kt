package uk.co.sentinelweb.cuer.app.ui.player

class PlayerPresenter(
    private val view: PlayerContract.View,
    private val state: PlayerContract.State,
    private val modelMapper: PlayerModelMapper
) : PlayerContract.Presenter {

}