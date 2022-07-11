package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.View.Event
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class SupportMviView constructor(
    private val log: LogWrapper,
    private val res: ResourceWrapper,
) : BaseMviView<Model, Event>(), SupportContract.View {

    init {
        log.tag(this)
    }

    // todo get list of links from description
    var observableModel: Model by mutableStateOf(Model(listOf()))
        private set

    private val _labelData: MutableLiveData<Label> = MutableLiveData()

    fun labelObservable(): LiveData<Label> = _labelData

    override suspend fun processLabel(label: Label) {
        _labelData.value = label
    }

    override fun render(model: Model) {
        log.d(model.toString())
        observableModel = model
    }
}
