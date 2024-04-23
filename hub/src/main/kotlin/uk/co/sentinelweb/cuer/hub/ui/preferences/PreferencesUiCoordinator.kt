package uk.co.sentinelweb.cuer.hub.ui.preferences

import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator

class PreferencesUiCoordinator(
    private val prefs: MultiPlatformPreferencesWrapper
) : UiCoordinator<PreferencesModel>,
    DesktopScopeComponent,
    KoinComponent {

    override val scope: Scope = desktopScopeWithSource(this)

    override val modelObservable = MutableStateFlow(PreferencesModel.blankModel())
    private val lifecycle: LifecycleRegistry by inject()
    private val mapper: PreferencesModelMapper by scope.inject()

    override fun create() {
        lifecycle.onCreate()
        lifecycle.onStart()
        lifecycle.onResume()
    }

    override fun destroy() {
        lifecycle.onPause()
        lifecycle.onStop()
        lifecycle.onDestroy()
        scope.close()
    }

    fun addFolder(path: String) {
        prefs.addFolderRoot(path)
        modelObservable.value = mapper.map()
    }

    fun removeFolder(path: String) {
        prefs.deleteFolderRoot(path)
        modelObservable.value = mapper.map()
    }

    companion object {
        @JvmStatic
        val uiModule = module {
            factory { PreferencesUiCoordinator(get()) }
            scope(named<PreferencesUiCoordinator>()) {
                scoped { PreferencesModelMapper(get()) }
            }
        }
    }
}
