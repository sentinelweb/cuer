package uk.co.sentinelweb.cuer.app.ui.browse

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.OnResume
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseFragment constructor() : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val controller: BrowseController by inject()
    private val log: LogWrapper by inject()
    private val browseMviView: BrowseMviView by inject()
    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        controller.onViewCreated(listOf(browseMviView), lifecycle.asMviLifecycle())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            BrowseUi(browseMviView)
        }
    }

    override fun onResume() {
        super.onResume()
        browseMviView.dispatch(OnResume)
    }

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<BrowseFragment>()) {
                scoped {
                    BrowseController(
                        storeFactory = get(),
                        modelMapper = get(),
                        lifecycle = getSource<BrowseFragment>().lifecycle.asMviLifecycle(),
                        log = get()
                    )
                }
                scoped {
                    BrowseStoreFactory(
                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        repository = get()
                    )
                }
                scoped { BrowseRepository() }
                scoped { BrowseModelMapper(get()) }
                scoped { BrowseMviView(get()) }
            }
        }
    }


}