package uk.co.sentinelweb.cuer.hub.ui.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesModel.Companion.blankModel
import uk.co.sentinelweb.cuer.hub.util.extension.DesktopScopeComponent
import uk.co.sentinelweb.cuer.hub.util.extension.desktopScopeWithSource
import uk.co.sentinelweb.cuer.hub.util.view.UiCoordinator
import java.io.File

class PreferencesUiCoordinator(
    private val prefs: MultiPlatformPreferencesWrapper
) : UiCoordinator<PreferencesModel>,
    DesktopScopeComponent,
    KoinComponent {
    override val scope: Scope = desktopScopeWithSource(this)

    private val mapper: PreferencesModelMapper by scope.inject()
    override val modelObservable = MutableStateFlow<PreferencesModel>(blankModel())

    override fun create() {
        modelObservable.value = mapper.map()
    }

    override fun destroy() {
        scope.close()
    }

    fun addFolder(path: String) {
        if (File(path).exists()) {
            prefs.addFolderRoot(path)
        }
        modelObservable.value = mapper.map()
    }

    fun removeFolder(path: String) {
        prefs.deleteFolderRoot(path)
        modelObservable.value = mapper.map()
    }

    fun setDataBaseInitialised(it: Boolean) {
        prefs.dbInitialised = it
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
