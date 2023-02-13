package uk.co.sentinelweb.cuer.app.ui.local

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View
import uk.co.sentinelweb.cuer.app.ui.local.LocalContract.View.Model
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class LocalMviViewProxy constructor(
    private val log: LogWrapper,
    private val res: ResourceWrapper,
) : BaseMviView<Model, View.Event>(), View {

    init {
        log.tag(this)
    }

    var observableModel: Model by mutableStateOf(Model("Remotes", null, localNode = null, address = null))
        private set

    var observableLoading: Boolean by mutableStateOf(false)
        private set

    private val _labelData: MutableLiveData<Label> = MutableLiveData()
    fun labelObservable(): LiveData<Label> = _labelData

    override fun processLabel(label: Label) {
        _labelData.value = label
    }

    override fun render(model: Model) {
        log.d(model.toString())
        observableModel = model
    }

    fun loading(isLoading: Boolean) {
        observableLoading = isLoading
    }
}
