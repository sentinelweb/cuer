package uk.co.sentinelweb.cuer.app.ui.player

import uk.co.sentinelweb.cuer.app.ui.player.PlayerModelMapper

class PlayerPresenter(
    private val view: PlayerContract.View,
    private val state: PlayerState,
    private val repository: PlayerRepository,
    private val modelMapper: PlayerModelMapper
) : PlayerContract.Presenter {

}