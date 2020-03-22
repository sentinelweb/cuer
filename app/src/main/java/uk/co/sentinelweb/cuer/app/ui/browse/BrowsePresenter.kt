package uk.co.sentinelweb.cuer.app.ui.browse


class BrowsePresenter(
    private val view: BrowseContract.View,
    private val state: BrowseState,
    private val repository: BrowseRepository,
    private val modelMapper: BrowseModelMapper
) : BrowseContract.Presenter {
    
}