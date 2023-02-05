package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviContract
import uk.co.sentinelweb.cuer.domain.*

interface PlaylistsMviDialogContract {
    enum class Label { Dismiss }

    data class Config(
        override val title: String,
        val selectedPlaylists: Set<PlaylistDomain>,
        val multi: Boolean,
        val itemClick: (PlaylistDomain?, Boolean) -> Unit,
        val confirm: (() -> Unit)?,
        val dismiss: () -> Unit,
        val suggestionsMedia: MediaDomain? = null,
        val showAdd: Boolean = true,
        val showPin: Boolean = true,
        val showRoot: Boolean = false
    ) : DialogModel(Type.PLAYLIST_FULL, title)

    data class State(
        var playlists: List<PlaylistDomain> = listOf(),
        var dragFrom: Int? = null,
        var dragTo: Int? = null,
        var playlistStats: List<PlaylistStatDomain> = listOf(),
        var channelPlaylistIds: MutableList<Identifier<GUID>> = mutableListOf(),
        var pinWhenSelected: Boolean = false,
        var playlistsModel: PlaylistsMviContract.View.Model? = null
    ) {
        lateinit var config: Config
        lateinit var treeRoot: PlaylistTreeDomain
    }

    data class Model(
        val playistsModel: PlaylistsMviContract.View.Model?,
        val showAdd: Boolean,
        val showPin: Boolean,
        val showUnPin: Boolean
    )

    open class Strings {
        open val playlists_section_channel = "For this channel"
        open val playlists_section_recent = "Recent"
        open val playlists_section_all = "All"
        open val playlists_dialog_title = "Select Playlist"
    }

    companion object {
        val ADD_PLAYLIST_DUMMY = PlaylistDomain.createDummy("Add Playlist")
        val ROOT_PLAYLIST_DUMMY = PlaylistDomain.createDummy("Top level")
    }
}