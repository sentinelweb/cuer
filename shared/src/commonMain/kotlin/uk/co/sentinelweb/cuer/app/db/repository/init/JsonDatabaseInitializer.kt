package uk.co.sentinelweb.cuer.app.db.init

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.backup.IBackupManager
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperation
import uk.co.sentinelweb.cuer.app.orchestrator.toLocalIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class JsonDatabaseInitializer constructor(
    private val assetOperator: AssetOperation,
    private val backup: IBackupManager,
    private val preferences: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) : DatabaseInitializer {

    init {
        log.tag(this)
    }

    private val listeners: MutableList<(Boolean) -> Unit> = mutableListOf()
    override fun isInitialized(): Boolean = preferences.dbInitialised

    override fun initDatabase() {
        coroutines.ioScope.launch {
            assetOperator.getAsString("default-dbinit.json")
                //res.getAssetString("default-dbinit.json")
                ?.apply { backup.restoreData(this) }
                ?.apply { preferences.dbInitialised = true }
                ?.apply { preferences.currentPlayingPlaylistId = 3L.toLocalIdentifier() /* philosophy */ }
                ?.apply { preferences.lastViewedPlaylistId = 3L.toLocalIdentifier() /* philosophy */ }
                ?.apply { log.d("DB initialised") }
                ?.apply { listeners.forEach { it.invoke(true) } }
        }
    }

    override fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

}