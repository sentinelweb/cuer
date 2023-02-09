package uk.co.sentinelweb.cuer.app.impl

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper

class Utils {

    fun log(): LogWrapper = SystemLogWrapper()

    fun lifecycleRegistry() = LifecycleRegistry()

    open class UBaseView<in Model : Any, Event : Any> : BaseMviView<Model, Event>()
}