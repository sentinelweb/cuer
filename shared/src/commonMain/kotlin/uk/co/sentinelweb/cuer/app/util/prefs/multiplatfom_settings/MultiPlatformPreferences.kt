package uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Companion.NO_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.orchestrator.toPair
import uk.co.sentinelweb.cuer.app.util.prefs.Field
import uk.co.sentinelweb.cuer.app.util.prefs.PrefWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.*
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.PLAYER_AUTO_FLOAT_DEFAULT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.Companion.RESTART_AFTER_UNLOCK_DEFAULT
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchLocal
import uk.co.sentinelweb.cuer.domain.ext.deserialiseSearchRemote
import uk.co.sentinelweb.cuer.domain.ext.serialise

enum class MultiPlatformPreferences constructor(override val fname: String) : Field {
    BROWSE_CAT_TITLE("browseNodeId"),
    BROWSE_RECENT_TITLES("browseRecent"),
    RECENT_PLAYLISTS("recentPlaylists"),
    FLOATING_PLAYER_RECT("floatingPlayerRect"),
    SHOW_VIDEO_CARDS("showVideoCards"),
    PLAYER_AUTO_FLOAT("playerAutoFloat"),
    RESTART_AFTER_UNLOCK("restartAfterUnlock"),
    BACKUP_LAST("lastBackup"),
    BACKUP_LAST_LOCATION("lastBackupLocation"),
    CURRENT_PLAYING_PLAYLIST_ID("currentPlaylistId"),
    CURRENT_PLAYING_PLAYLIST_SOURCE("currentPlaylistSource"),
    LAST_PLAYLIST_VIEWED_ID("lastViewedPlaylistId"),
    LAST_PLAYLIST_VIEWED_SOURCE("lastViewedPlaylistSource"),
    LAST_PLAYLIST_ADDED_TO("lastPlaylistAddedTo"),
    PINNED_PLAYLIST("pinnedPlaylist"),
    LAST_LOCAL_SEARCH("lastLocalSearch"),
    LAST_REMOTE_SEARCH("lastRemoteSearch"),
    LAST_SEARCH_TYPE("lastSearchType"),
    DB_INITIALISED("dbInitialised"),
    LAST_BOTTOM_TAB("lastBottomNavTab"),
    ;

    companion object {
        const val PLAYER_AUTO_FLOAT_DEFAULT = true
        const val RESTART_AFTER_UNLOCK_DEFAULT = true
    }
}


interface MultiPlatformPreferencesWrapper : PrefWrapper<MultiPlatformPreferences> {
    var playerAutoFloat: Boolean
        get() = getBoolean(PLAYER_AUTO_FLOAT, PLAYER_AUTO_FLOAT_DEFAULT)
        set(value) = putBoolean(PLAYER_AUTO_FLOAT, value)

    var restartAfterUnlock: Boolean
        get() = getBoolean(RESTART_AFTER_UNLOCK, RESTART_AFTER_UNLOCK_DEFAULT)
        set(value) = putBoolean(RESTART_AFTER_UNLOCK, value)

    val removeLastBackupData: Boolean
        get() {
            remove(BACKUP_LAST)
            remove(BACKUP_LAST_LOCATION)
            return true
        }

    var dbInitialised: Boolean
        get() = getBoolean(DB_INITIALISED, false)
        set(value) = putBoolean(DB_INITIALISED, value)

    var lastBackupInstant: Instant?
        get() = getString(BACKUP_LAST, null)
            ?.let { Instant.parse(it) }
        set(value) = putString(BACKUP_LAST, value.toString())

    var lastBackupLocation: String?
        get() = getString(BACKUP_LAST_LOCATION, null)
        set(value) = value
            ?.let { putString(BACKUP_LAST_LOCATION, it) }
            ?: let { remove(BACKUP_LAST_LOCATION) }

    var currentPlayingPlaylistId: OrchestratorContract.Identifier<GUID>
        get() = if (has(CURRENT_PLAYING_PLAYLIST_ID) && has(CURRENT_PLAYING_PLAYLIST_ID)) {
            (getString(CURRENT_PLAYING_PLAYLIST_ID, null)!!.toGUID()
                    to Source.valueOf(getString(CURRENT_PLAYING_PLAYLIST_SOURCE, null)!!)
                    ).toIdentifier()
        } else NO_PLAYLIST.toPair().toIdentifier()
        set(value) = value
            .let {
                putString(CURRENT_PLAYING_PLAYLIST_ID, it.id.value)
                putString(CURRENT_PLAYING_PLAYLIST_SOURCE, it.source.toString())
            }

    var lastViewedPlaylistId: OrchestratorContract.Identifier<GUID>
        get() = if (has(LAST_PLAYLIST_VIEWED_ID) && has(LAST_PLAYLIST_VIEWED_ID)) {
            (getString(LAST_PLAYLIST_VIEWED_ID, null)!!.toGUID()
                    to Source.valueOf(getString(LAST_PLAYLIST_VIEWED_SOURCE, null)!!)
                    ).toIdentifier()
        } else NO_PLAYLIST.toPair().toIdentifier()
        set(value) = value
            .let {
                putString(LAST_PLAYLIST_VIEWED_ID, it.id.value)
                putString(LAST_PLAYLIST_VIEWED_SOURCE, it.source.toString())
            }
//        get() = getPair(LAST_PLAYLIST_VIEWED, NO_PLAYLIST.toPair()).toIdentifier()
//        set(value) = value
//            .let { putPair(LAST_PLAYLIST_VIEWED, it.toPair()) }

    var lastAddedPlaylistId: GUID?
        get() = getString(LAST_PLAYLIST_ADDED_TO, null)?.toGUID()
        set(value) = value
            ?.let { putString(LAST_PLAYLIST_ADDED_TO, it.value) }
            ?: let { remove(LAST_PLAYLIST_ADDED_TO) }

    var pinnedPlaylistId: GUID?
        get() = getString(PINNED_PLAYLIST, null)?.toGUID()
        set(value) = value
            ?.let { putString(PINNED_PLAYLIST, it.value) }
            ?: let { remove(PINNED_PLAYLIST) }

    var lastLocalSearch: SearchLocalDomain?
        get() = getString(LAST_LOCAL_SEARCH, null)
            ?.let { deserialiseSearchLocal(it) }
        set(value) = value
            ?.let { putString(LAST_LOCAL_SEARCH, it.serialise()) }
            ?: let { remove(LAST_LOCAL_SEARCH) }

    val hasLocalSearch: Boolean get() = getString(LAST_LOCAL_SEARCH, null) != null
    var lastRemoteSearch: SearchRemoteDomain?
        get() = getString(LAST_REMOTE_SEARCH, null)
            ?.let { deserialiseSearchRemote(it) }
        set(value) = value
            ?.let { putString(LAST_REMOTE_SEARCH, it.serialise()) }
            ?: let { remove(LAST_REMOTE_SEARCH) }
    val hasRemoteSearch: Boolean get() = getString(LAST_REMOTE_SEARCH, null) != null

    var lastSearchType: SearchTypeDomain?
        get() = getString(LAST_SEARCH_TYPE, null)
            ?.let { SearchTypeDomain.valueOf(it) }
        set(value) = value
            ?.let { putString(LAST_SEARCH_TYPE, it.toString()) }
            ?: let { remove(LAST_SEARCH_TYPE) }


    var recentIds: String?
        get() = getString(RECENT_PLAYLISTS, null)
        set(value) = value
            ?.let { putString(RECENT_PLAYLISTS, it) }
            ?: let { remove(RECENT_PLAYLISTS) }

    var lastBottomTab: Int
        get() = getInt(LAST_BOTTOM_TAB) ?: 0
        set(value) = value
            .let { putInt(LAST_BOTTOM_TAB, it) }

}