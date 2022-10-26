package uk.co.sentinelweb.cuer.app.ui.common.dialog.support

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.arkivanov.essenty.lifecycle.asEssentyLifecycle
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.MEDIA
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.common.navigation.navigationRouter
import uk.co.sentinelweb.cuer.app.ui.share.ShareNavigationHack
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.Label.Crypto
import uk.co.sentinelweb.cuer.app.ui.support.SupportContract.MviStore.Label.Open
import uk.co.sentinelweb.cuer.app.ui.support.SupportController
import uk.co.sentinelweb.cuer.app.ui.support.SupportModelMapper
import uk.co.sentinelweb.cuer.app.ui.support.SupportStoreFactory
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.LinkDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseMedia
import uk.co.sentinelweb.cuer.domain.ext.serialise

class SupportDialogFragment : DialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<SupportDialogFragment>()
    private val controller: SupportController by inject()
    private val mviView: SupportMviView by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val cryptoLauncher: CryptoLauncher by inject()
    private val toast: ToastWrapper by inject()
    private val shareNavigationHack: ShareNavigationHack by inject()
    private val navRouter: NavigationRouter by inject()
    private val linkScanner: LinkScanner by inject()
//    private val urlLauncher: UrlLauncherWrapper by inject()
//    private val ytLauncher: YoutubeJavaApiWrapper by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    private val media: MediaDomain? by lazy {
        arguments?.getString(MEDIA.toString())?.let { deserialiseMedia(it) }
    }

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        controller.onViewCreated(listOf(mviView), lifecycle.asEssentyLifecycle())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        media?.also {
            coroutines.mainScope.launch {
                delay(300)
                mviView.dispatch(SupportContract.View.Event.Load(it))
            }
        } ?: run {
            toast.show("Can't load media")
            dismissAllowingStateLoss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    private fun observeLabels() {
        mviView.labelObservable().observe(this.viewLifecycleOwner, object : Observer<SupportContract.MviStore.Label> {
            override fun onChanged(label: SupportContract.MviStore.Label) {
                when (label) {
                    is Open, is Crypto -> shareNavigationHack.isNavigatingInApp = true
                }
                when (label) {
                    is Open -> (
                            linkScanner.scan(label.link.address)
                                ?.let { scanned ->
                                    when (scanned.first) {
                                        ObjectTypeDomain.MEDIA -> navShare(label.link)
                                        ObjectTypeDomain.PLAYLIST -> navShare(label.link)
                                        ObjectTypeDomain.PLAYLIST_ITEM -> navShare(label.link)
                                        ObjectTypeDomain.CHANNEL -> navLink(label.link)
                                        else -> navLink(label.link)
                                    }
                                }
                                ?: let { navLink(label.link) }
                            ).apply { navRouter.navigate(this) }

//                        when (label.link.domain) {
//                            YOUTUBE -> ytLauncher.launch(label.link.address)
//                            else -> urlLauncher.launchUrl(label.link.address)
//                        }

                    is Crypto -> cryptoLauncher.launch(label.link)
                }
            }
        })
    }


    private fun navLink(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(NavigationModel.Target.WEB_LINK, mapOf(NavigationModel.Param.LINK to link.address))

    private fun navShare(link: LinkDomain.UrlLinkDomain): NavigationModel =
        NavigationModel(NavigationModel.Target.SHARE, mapOf(NavigationModel.Param.LINK to link.address))


    class SupportStrings(private val res: ResourceWrapper) : SupportContract.Strings

    companion object {
        const val TAG = "SupportDialogFragment"

        // todo use navigation?
        fun show(a: FragmentActivity, m: MediaDomain) {
            SupportDialogFragment().apply { arguments = bundleOf(MEDIA.toString() to m.serialise()) }
                .show(a.supportFragmentManager, TAG)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<SupportDialogFragment>()) {
                scoped {
                    SupportController(
                        storeFactory = get(), modelMapper = get()
                    )
                }
                scoped {
                    SupportStoreFactory(
//                        storeFactory = LoggingStoreFactory(DefaultStoreFactory),
                        storeFactory = DefaultStoreFactory(), log = get(), prefs = get(), linkExtractor = get()
                    )
                }
                scoped<SupportContract.Strings> { SupportStrings(get()) }
                scoped { SupportModelMapper() }
                scoped { SupportMviView(get(), get()) }
                scoped { UrlLauncherWrapper(this.getFragmentActivity()) }
                scoped { YoutubeJavaApiWrapper(this.getFragmentActivity(), get()) }
                scoped<CryptoLauncher> {
                    AndroidCryptoLauncher(this.getFragmentActivity(), get(), get(), get())
                }
                scoped { navigationRouter(true, this.getFragmentActivity()) }
            }
        }
    }
}
