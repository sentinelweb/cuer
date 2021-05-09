package uk.co.sentinelweb.cuer.app.ui.search.image

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.common.dialog.EnumValuesDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.domain.ImageDomain

interface SearchImageContract {

    data class Model constructor(
        val term: String?,
        val images: List<ImageDomain>
    )

    data class State constructor(
        var term: String? = null,
        var images: List<ImageDomain>? = null,
        var config: Config? = null
    )

    data class Config(
        val initialTerm: String?,
        val itemClick: (ImageDomain) -> Unit
    ) : DialogModel(Type.IMAGE_SEARCH, R.string.imagesearch_dialog_title)

    class Mapper {
        fun map(state: State) = Model(
            term = state.term,
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
                        pixabayInteractor = get()
                    )
                }
                scoped { State() }
                scoped { navigationMapper(true, getSource<Fragment>().requireActivity() as AppCompatActivity) }
                scoped { EnumValuesDialogCreator(getSource<Fragment>().requireContext()) }
            }
            factory { Mapper() }
        }

    }
}