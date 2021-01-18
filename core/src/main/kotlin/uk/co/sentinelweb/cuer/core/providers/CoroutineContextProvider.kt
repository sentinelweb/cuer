package uk.co.sentinelweb.cuer.core.providers

import kotlinx.coroutines.*
import java.util.concurrent.Executors

// this should be factory scopes should be shared between objects
open class CoroutineContextProvider {
    open val Main: CoroutineDispatcher = Dispatchers.Main
    open val IO: CoroutineDispatcher = Dispatchers.IO
    open val Default: CoroutineDispatcher = Dispatchers.Default

    inner class ScopeHolder(private val dispatcher: CoroutineDispatcher) {
        private var _scope: CoroutineScope? = null
        fun get(): CoroutineScope {
            return _scope ?: CoroutineScope(dispatcher).apply { _scope = this }
        }

        fun cancel() {
            _scope?.cancel()
            _scope = null
        }
    }

    private var _mainScope = ScopeHolder(Main)
    val mainScope: CoroutineScope
        get() = _mainScope.get()

    private var _computationScope = ScopeHolder(Computation)
    val computationScope: CoroutineScope
        get() = _computationScope.get()

    private var _ioScope = ScopeHolder(IO)
    val ioScope: CoroutineScope
        get() = _ioScope.get()

    fun cancel() {
        _mainScope.cancel()
        _computationScope.cancel()
        _ioScope.cancel()
    }

    companion object {
        val Computation: CoroutineDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    }
}