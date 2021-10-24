package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseMviView constructor(
    private val log: LogWrapper,
    private val res: ResourceWrapper,
) : BaseMviView<Model, Event>(), BrowseContract.View {

    init {
        log.tag(this)
    }

    var observableModel: Model by mutableStateOf(Model(res.getString(R.string.bottomnav_title_browse), listOf(), true))
        private set

    private val _labelData: MutableLiveData<Label> = MutableLiveData()
    fun labelObservable(): LiveData<Label> = _labelData

    override suspend fun processLabel(label: Label) {
        _labelData.value = label
        log.d("Got label: $label")
    }

    override fun render(model: Model) {
        log.d(model.toString())
        observableModel = model
    }

}