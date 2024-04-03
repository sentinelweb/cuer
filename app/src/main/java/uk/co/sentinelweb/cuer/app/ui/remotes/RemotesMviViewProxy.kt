package uk.co.sentinelweb.cuer.app.ui.remotes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.remotes.RemotesModelMapper.Companion.blankModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class RemotesMviViewProxy(
    private val log: LogWrapper,
    private val res: ResourceWrapper,
) : BaseMviView<Model, Event>(), RemotesContract.View {

    init {
        log.tag(this)
    }

    var observableModel: Model by mutableStateOf(blankModel())
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

    }
}
