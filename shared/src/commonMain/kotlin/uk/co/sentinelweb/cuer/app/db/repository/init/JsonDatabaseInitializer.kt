package uk.co.sentinelweb.cuer.app.db.init

import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.backup.IBackupJsonManager
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.usecase.PlaylistUpdateUsecase
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class JsonDatabaseInitializer constructor(
    private val assetOperations: AssetOperations,
    private val backup: IBackupJsonManager,
    private val preferences: MultiPlatformPreferencesWrapper,
    private val coroutines: CoroutineContextProvider,
    private val playlistUpdateUsecase: PlaylistUpdateUsecase,
    private val recentLocalPlaylists: RecentLocalPlaylists,
    private val log: LogWrapper,
) : DatabaseInitializer {

    init {
        log.tag(this)
    }

    private val listeners: MutableList<(Boolean) -> Unit> = mutableListOf()
    override fun isInitialized(): Boolean = preferences.dbInitialised

    override fun initDatabase(path: String) {
        coroutines.ioScope.launch {
            assetOperations.getAsString(path)
                ?.apply { backup.restoreData(this) }
                ?.apply { preferences.dbInitialised = true }
                ?.apply { playlistUpdateUsecase.update(DatabaseInitializer.PHILOSOPHY_PLAYLIST_ID) }
//                ?.apply { preferences.currentPlayingPlaylistId = DatabaseInitializer.PHILOSOPHY_PLAYLIST_ID }
                ?.apply { preferences.lastViewedPlaylistId = DatabaseInitializer.PHILOSOPHY_PLAYLIST_ID }
                ?.apply { recentLocalPlaylists.addRecentId(DatabaseInitializer.PHILOSOPHY_PLAYLIST_ID.id) }
                ?.apply { log.d("DB initialised") }
                ?.apply { listeners.forEach { it.invoke(true) } }
        }
    }

    override fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

}