package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.MviStore.Label
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseMviView constructor(
    private val log: LogWrapper,
) : BaseMviView<Model, Event>(), BrowseContract.View {

    init {
        log.tag(this)
    }

    var observableModel: Model by mutableStateOf(Model("Default title", listOf(), true))
        private set

    var observableLabel: Label by mutableStateOf(Label.None)
        private set

    override suspend fun processLabel(label: Label) {
        observableLabel = label // todo observe for side effects
        log.d("Got label: $label")
    }

    override fun render(model: Model) {
        log.d(model.toString())
        observableModel = model
    }

}