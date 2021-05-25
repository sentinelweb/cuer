package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.*
import java.util.concurrent.Executors

// this should be factory scopes should be shared between objects
open class CoroutineContextProvider constructor(
    val Main: CoroutineDispatcher = Dispatchers.Main,
    val IO: CoroutineDispatcher = Dispatchers.IO,
    val Default: CoroutineDispatcher = Dispatchers.Default,
    val Computation: CoroutineDispatcher = ComputationDispatcher
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

    companion object {
        val ComputationDispatcher: CoroutineDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    }
}