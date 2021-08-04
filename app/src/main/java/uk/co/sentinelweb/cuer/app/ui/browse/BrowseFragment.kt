package uk.co.sentinelweb.cuer.app.ui.browse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseContract.View.Event.OnResume
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class BrowseFragment constructor() : Fragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val controller: BrowseController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val browseMviView: BrowseMviView by inject()
    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    // saves the data on back press (enabled in onResume)
    private val upCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            browseMviView.dispatch(BrowseContract.View.Event.UpClicked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        controller.onViewCreated(listOf(browseMviView), lifecycle.asMviLifecycle())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            BrowseComposables.BrowseUi(browseMviView)
        }
        coroutines.mainScope.launch {
            delay(300)
            browseMviView.dispatch(OnResume)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, upCallback)
        linkScopeToActivity()
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