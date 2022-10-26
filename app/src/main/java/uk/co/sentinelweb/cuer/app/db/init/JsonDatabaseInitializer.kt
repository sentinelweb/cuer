package uk.co.sentinelweb.cuer.app.db.init

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.backup.IBackupManager
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class JsonDatabaseInitializer constructor(
    private val res: ResourceWrapper,
    private val backup: IBackupManager,
    private val preferences: GeneralPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val log: LogWrapper,
) : DatabaseInitializer {

    init {
        log.tag(this)
    }

    private val listeners: MutableList<(Boolean) -> Unit> = mutableListOf()
    override fun isInitialized(): Boolean = preferences.getBoolean(DB_INITIALISED, false)

    override fun initDatabase() {
        coroutines.ioScope.launch {
            res.getAssetString("default-080721.json")
                ?.apply { backup.restoreData(this) }
                ?.apply { preferences.putBoolean(DB_INITIALISED, true) }
                ?.apply { preferences.putPair(CURRENT_PLAYING_PLAYLIST, 3L to LOCAL) /* philosophy */ }
                ?.apply { preferences.putPair(LAST_PLAYLIST_VIEWED, 3L to LOCAL) /* philosophy */ }
                ?.apply { log.d("DB initialised") }
                ?.apply { listeners.forEach { it.invoke(true) } }
        }
    }

    override fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

}