package uk.co.sentinelweb.cuer.app.ui.playlist

class PlaylistPresenter(
    private val view: PlaylistContract.View,
    private val state: PlaylistState,
    private val repository: PlaylistRepository,
    private val modelMapper: PlaylistModelMapper
) : PlaylistContract.Presenter {
}