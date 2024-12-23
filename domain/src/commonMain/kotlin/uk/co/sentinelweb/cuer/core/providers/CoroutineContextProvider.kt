package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.*


interface DispatcherProvider {
    val Main: CoroutineDispatcher
    val IO: CoroutineDispatcher
    val Computation: CoroutineDispatcher
}
// should be single
// todo review this - maybe just inject CoroutineDispatcher with enums?
expect object PlatformDispatcherProvider : DispatcherProvider

open class CoroutineContextProvider constructor(
    val Main: CoroutineDispatcher = PlatformDispatcherProvider.Main,
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

    private var _defaultScope = ScopeHolder(Default)

    val defaultScope: CoroutineScope
        get() = _defaultScope.get()

    val defaultScopeActive: Boolean
        get() = _defaultScope.isActive()


    fun cancel() {
        _mainScope.cancel()
        _computationScope.cancel()
        _ioScope.cancel()
        _defaultScope.cancel()
    }
}

@Suppress("unused")
fun Job.ignoreJob() = Unit