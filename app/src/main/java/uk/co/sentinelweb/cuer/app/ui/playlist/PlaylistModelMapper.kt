package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain
import java.util.*


class PlaylistModelMapper constructor(
    private val itemModelMapper: ItemModelMapper,
    private val iconMapper: IconMapper,
    private val res: ResourceWrapper
) {
    private var _modelIdGenerator = 0L
    var modelIdGenerator: Long = 0
        get() {
            _modelIdGenerator--
            return _modelIdGenerator
        }
        set(value) = if (value != 0L) {
            throw IllegalArgumentException("You can only reset the generator")
        } else field = value

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        mapItems: Boolean = true,
        id: OrchestratorContract.Identifier<*>,
        pinned: Boolean,
        playlists: Map<Long, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?,
    ): PlaylistContract.Model {
        modelIdGenerator = 0
        val itemsIdMap = mutableMapOf<Long, PlaylistItemDomain>()
        return PlaylistContract.Model(
            title = domain.title.capitalize(Locale.getDefault()),
            imageUrl = (domain.image ?: domain.thumb)?.url
                ?: "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
            loopModeIndex = domain.mode.ordinal,
            loopModeIcon = iconMapper.map(domain.mode),
            loopModeText = when (domain.mode) {
                PlaylistDomain.PlaylistModeDomain.SINGLE -> res.getString(R.string.menu_playlist_mode_single)
                PlaylistDomain.PlaylistModeDomain.LOOP -> res.getString(R.string.menu_playlist_mode_loop)
                PlaylistDomain.PlaylistModeDomain.SHUFFLE -> res.getString(R.string.menu_playlist_mode_shuffle)
            },
            playIcon = if (isPlaying) R.drawable.ic_playlist_close else R.drawable.ic_playlist_play,
            playText = res.getString(if (isPlaying) R.string.stop else R.string.menu_play),
            starredIcon = if (domain.starred) R.drawable.ic_starred else R.drawable.ic_starred_off,
            starredText = res.getString(if (domain.starred) R.string.menu_star else R.string.menu_unstar),
            isDefault = domain.default,
            isSaved = id.source == LOCAL,
            isPlayFromStart = domain.playItemsFromStart,
            isPinned = pinned,
            canPlay = domain.config.playable,
            canEdit = domain.config.editable,
            items = if (mapItems) {
                domain.items.mapIndexed { index, item ->
                    val modelId = item.id ?: modelIdGenerator
                    itemsIdMap[modelId] = item
                    itemModelMapper.mapItem(
                        modelId,
                        item,
                        index,
                        domain.config.editableItems,
                        domain.config.deletableItems,
                        domain.config.editable,
                        mapPlaylistText(item, domain, playlists),
                        true,
                        appPlaylist?.customResources?.customDelete
                    )
                }
            } else {
                null
            },
            hasChildren = playlists?.get(domain.id)?.chidren?.size ?: 0,
            itemsIdMap = itemsIdMap
        )
    }

    fun mapPlaylistText(
        item: PlaylistItemDomain,
        domain: PlaylistDomain?,
        playlists: Map<Long, PlaylistTreeDomain>?
    ): String? {
        val playlistText = item.playlistId?.let { itemPlaylistId ->
            if (itemPlaylistId != domain?.id) playlists?.get(itemPlaylistId)?.node?.title else null
        }
        return playlistText
    }

    fun mapSaveConfirmAlert(confirm: () -> Unit, cancel: () -> Unit): AlertDialogModel =
        AlertDialogModel(
            title = res.getString(R.string.dialog_title_save_check),
            message = res.getString(R.string.dialog_message_save_item_check),
            confirm = AlertDialogModel.Button(R.string.dialog_button_save, confirm),
            cancel = AlertDialogModel.Button(R.string.dialog_button_dont_save, cancel),
        )

}
