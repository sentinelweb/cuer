package uk.co.sentinelweb.cuer.hub.util.view

import kotlinx.coroutines.flow.Flow

interface UiCoordinator<Model : Any> {
    val modelObservable: Flow<Model>
    fun create()
    fun destroy()
}