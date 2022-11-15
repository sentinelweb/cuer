package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel


interface DispatcherProvider {
    val IO: CoroutineDispatcher
    val Computation: CoroutineDispatcher
}

expect object PlatformDispatcherProvider : DispatcherProvider

open class CoroutineContextProvider constructor(
    val Main: CoroutineDispatcher = Dispatchers.Main,
    val IO: CoroutineDispatcher = PlatformDispatcherProvider.IO,
    val Default: CoroutineDispatcher = Dispatchers.Default,
    val Computation: CoroutineDispatcher = PlatformDispatcherProvider.Computation
) {
    inner class ScopeHolder(private val dispatcher: CoroutineDispatcher) {
        private var _scope: CoroutineScope? = null
        fun get(): CoroutineScope {
            return _scope ?: CoroutineScope(dispatcher).apply { _scope = this }
        }

        fun isActive() = _scope != null

        fun cancel() {
            _scope?.cancel()
            _scope = null
        }
    }

    private var _mainScope = ScopeHolder(Main)
    val mainScope: CoroutineScope
        get() = _mainScope.get()

    val mainScopeActive: Boolean
        get() = _mainScope.isActive()

    private var _computationScope = ScopeHolder(Computation)
    val computationScope: CoroutineScope
        get() = _computationScope.get()

    val computationScopeActive: Boolean
        get() = _computationScope.isActive()

    private var _ioScope = ScopeHolder(IO)
    val ioScope: CoroutineScope
        get() = _ioScope.get()

    val ioScopeActive: Boolean
        get() = _ioScope.isActive()

    fun cancel() {
        _mainScope.cancel()
        _computationScope.cancel()
        _ioScope.cancel()
    }
}