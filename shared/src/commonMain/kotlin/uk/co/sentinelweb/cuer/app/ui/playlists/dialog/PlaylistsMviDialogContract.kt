package uk.co.sentinelweb.cuer.app.ui.playlists.dialog

import uk.co.sentinelweb.cuer.app.ui.common.dialog.DialogModel
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistsMviDialogContract {
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

}