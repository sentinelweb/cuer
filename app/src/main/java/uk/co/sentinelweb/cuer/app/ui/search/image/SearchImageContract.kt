package uk.co.sentinelweb.cuer.app.ui.search.image

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.EnumValuesDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.image.ImageSelectIntentHandler
import uk.co.sentinelweb.cuer.domain.ImageDomain

interface SearchImageContract {

    data class SearchModel constructor(
        val term: String?,
        val loading: Boolean
    )

    data class ResultsModel constructor(
        val images: List<ImageDomain>
    )

    data class State constructor(
        var term: String? = null,
        var images: List<ImageDomain>? = null,
        var config: Config? = null,
        var loading: Boolean = false
    )

    data class Config(
        val initialTerm: String?,
        val itemClick: (ImageDomain) -> Unit
    ) : DialogModel(Type.IMAGE_SEARCH, R.string.imagesearch_dialog_title)

    class Mapper {
        fun mapSearch(state: State) = SearchModel(
            term = state.term,
            loading = state.loading
        )

        fun mapResults(state: State) = ResultsModel(
            images = state.images ?: listOf()
        )
    }

    companion object {
        @JvmStatic
        val fragmentModule = module {
            scope(named<SearchImageDialogFragment>()) {
                viewModel {
                    SearchImageViewModel(
                        state = get(),
                        log = get(),
                        mapper = get(),
                        pixabayInteractor = get(),
                        imageFileRepository = get()
                    )
                }
                scoped { State() }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
                scoped { EnumValuesDialogCreator(this.getFragmentActivity()) }
                scoped {
                    ImageSelectIntentHandler(
                        a = this.getFragmentActivity(), res = get(), bitmapSizer = get()
                    )
                }
                scoped { AlertDialogCreator(this.getFragmentActivity()) }
            }
            factory { Mapper() }
        }
    }
}