package uk.co.sentinelweb.cuer.app.ui.browse

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.arkivanov.mvikotlin.core.view.BaseMviView
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Model
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseMviView constructor(
    private val log: LogWrapper,
) : BaseMviView<Model, Event>(), BrowseContract.View {
    init {
        log.tag(this)
    }

    var observableModel: Model by mutableStateOf(
//        BrowseModelMapper(log).map(BrowseTestData.state)
        Model(categories = listOf())
    )
        private set

    override fun render(model: Model) {
        log.d(model.toString())
        observableModel = model
    }

}