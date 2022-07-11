package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationMapper
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.Label.Open
import uk.co.sentinelweb.cuer.app.ui.support.SupportController
import uk.co.sentinelweb.cuer.app.ui.support.SupportModelMapper
import uk.co.sentinelweb.cuer.app.ui.support.SupportStoreFactory
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class SupportDialogFragment(
    private val media: MediaDomain
) : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val controller: SupportController by inject()
    private val mviView: SupportMviView by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        controller.onViewCreated(listOf(mviView), lifecycle.asMviLifecycle())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            SupportComposables.SupportUi(mviView)
        }
        observeLabels()
        mviView.dispatch(SupportContract.View.Event.Load(media))
    }

    private fun observeLabels() {
        mviView.labelObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<SupportContract.MviStore.Label> {
                override fun onChanged(label: SupportContract.MviStore.Label) {
                    when (label) {
                        is Open -> log.d("open: ${label.url}")
                    }
                }
            })
    }

    class SupportStrings(private val res: ResourceWrapper) : SupportContract.Strings {

    }

    companion object {
        val TAG = "SupportDialogFragment"

        // todo use navigation?
        fun show(a: FragmentActivity, m: MediaDomain) {
            SupportDialogFragment(m)
                .show(a.supportFragmentManager, TAG)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<SupportDialogFragment>()) {
                scoped {
                    SupportController(
                        storeFactory = get(),
                        modelMapper = get()
                    )
                }
                scoped {
                    SupportStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory,
                        log = get(),
                        prefs = get(),
                        linkExtractor = get()
                    )
                }
                scoped<SupportContract.Strings> { SupportStrings(get()) }
                scoped { SupportModelMapper() }
                scoped { SupportMviView(get(), get()) }
                scoped { navigationMapper(true, this.getFragmentActivity()) }
            }
        }
    }
}