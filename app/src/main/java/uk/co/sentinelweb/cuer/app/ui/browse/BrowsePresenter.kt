package uk.co.sentinelweb.cuer.app.ui.browse


class BrowsePresenter(
    private val view: BrowseContract.View,
    private val state: BrowseContract.State,
    private val repository: BrowseRepository,
    private val modelMapper: BrowseModelMapper
) : BrowseContract.Presenter {
    
}