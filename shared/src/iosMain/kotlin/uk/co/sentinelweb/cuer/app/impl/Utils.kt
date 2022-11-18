package uk.co.sentinelweb.cuer.app.impl

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper

class Utils {

    fun log(): LogWrapper = SystemLogWrapper()

    fun lifecycleRegistry() = LifecycleRegistry()

    // fixme this isnt the sale as LifecycleRegistryExtKt.destroy(lifecycle) in mvi sample
    fun destroyLifecycle(lifecycleReg: LifecycleRegistry, lifecycle: Lifecycle) {
        lifecycleReg.destroy()
        lifecycle.unsubscribe(object : Lifecycle.Callbacks {}) // doesnt do anything
    }

    interface ULifeycle : Lifecycle

    open class UBaseView<in Model : Any, Event : Any> : BaseMviView<Model, Event>()
}