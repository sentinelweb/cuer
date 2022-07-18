package uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.APP_LIST
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.getFragmentActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.AppLauncherWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.AppListBuilder

class AppSelectorBottomSheet : BottomSheetDialogFragment(), AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val appLauncher: AppLauncherWrapper by inject()
    private val appListBuilder: AppListBuilder by inject()

    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Not bound")

    private val appList: List<String> by lazy {
        arguments?.getStringArray(APP_LIST.toString())
            ?.toList()
            ?: throw IllegalArgumentException("No $APP_LIST")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        binding.composeView.setContent {
            AppSelectComposables.AppSelectView(
                apps = appListBuilder.buildAppList(appList),
                onClick = { appLauncher.launchApp(it) }
            )
        }
        return binding.root
    }

    companion object {
        val TAG = "AppSelectorBottomSheet"

        // todo use navigation?
        fun show(a: FragmentActivity, appList: List<String>) {
            AppSelectorBottomSheet()
                .apply {
                    arguments = Bundle().apply {
                        putStringArray(APP_LIST.toString(), appList.toTypedArray())
                    }
                }
                .show(a.supportFragmentManager, TAG)
        }

        @JvmStatic
        val fragmentModule = module {
            scope(named<AppSelectorBottomSheet>()) {
                scoped { AppLauncherWrapper(this.getFragmentActivity()) }
            }
        }
    }
}