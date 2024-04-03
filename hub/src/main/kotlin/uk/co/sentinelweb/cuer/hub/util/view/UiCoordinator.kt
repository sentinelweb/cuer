package uk.co.sentinelweb.cuer.hub.util.view

interface UiCoordinator<Model : Any> {
    fun create()
    fun destroy()

    fun observeModel(updater: (Model) -> Unit)
}