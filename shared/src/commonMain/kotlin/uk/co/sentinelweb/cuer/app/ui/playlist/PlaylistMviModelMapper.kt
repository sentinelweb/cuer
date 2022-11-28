package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain


class PlaylistMviModelMapper constructor(
    private val itemModelMapper: PlaylistMviItemModelMapper,
    private val iconMapper: IconMapper,
    private val stringDecoder: StringDecoder,
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor>,
    private val util: PlaylistMviUtil
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

    fun map(state: PlaylistMviContract.MviStore.State): PlaylistMviContract.View.Model =
        state.playlist?.let { playlist ->
            map(
                domain = playlist,
                isPlaying = util.isPlaylistPlaying(state),
                id = state.playlistIdentifier,
                playlists = state.playlistsTreeLookup,
                pinned = util.isPlaylistPinned(state),
                appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] }

            )
        } ?: PlaylistMviContract.View.Model(
            title = "Empty",
            imageUrl = "https://cuer-275020.firebaseapp.com/images/headers/headphones-2588235_640.jpg",
            loopModeIndex = 0,
            loopModeIcon = Icon.ic_playmode_straight,
            loopModeText = stringDecoder.getString(StringResource.menu_playlist_mode_single),
            playIcon = Icon.ic_playlist_play,
            playText = stringDecoder.getString(StringResource.stop),
            starredIcon = Icon.ic_starred_off,
            starredText = "",
            isStarred = false,
            isDefault = false,
            isSaved = false,
            isPlayFromStart = false,
            isPinned = false,
            canPlay = false,
            canEdit = false,
            canDelete = false,
            canEditItems = false,
            canDeleteItems = false,
            items = listOf(),
            hasChildren = 0,
            itemsIdMap = mutableMapOf(),
            canUpdate = false,
        )

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        mapItems: Boolean = true,
        id: OrchestratorContract.Identifier<*>,
        pinned: Boolean,
        playlists: Map<Long, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?,
    ): PlaylistMviContract.View.Model {
        modelIdGenerator = 0
        val itemsIdMap = mutableMapOf<Long, PlaylistItemDomain>()
        return PlaylistMviContract.View.Model(
            title = domain.title.capitalize(),
            imageUrl = (domain.image ?: domain.thumb)?.url
                ?: "gs://cuer-275020.appspot.com/playlist_header/headphones-2588235_640.jpg",
            loopModeIndex = domain.mode.ordinal,
            loopModeIcon = iconMapper.map(domain.mode),
            loopModeText = when (domain.mode) {
                PlaylistDomain.PlaylistModeDomain.SINGLE -> stringDecoder.getString(StringResource.menu_playlist_mode_single)
                PlaylistDomain.PlaylistModeDomain.LOOP -> stringDecoder.getString(StringResource.menu_playlist_mode_loop)
                PlaylistDomain.PlaylistModeDomain.SHUFFLE -> stringDecoder.getString(StringResource.menu_playlist_mode_shuffle)
            },
            playIcon = if (isPlaying) Icon.ic_playlist_close else Icon.ic_playlist_play,
            playText = stringDecoder.getString(if (isPlaying) StringResource.stop else StringResource.menu_play),
            starredIcon = if (domain.starred) Icon.ic_starred else Icon.ic_starred_off,
            starredText = stringDecoder.getString(if (domain.starred) StringResource.menu_unstar else StringResource.menu_star),
            isStarred = domain.starred,
            isDefault = domain.default,
            isSaved = id.source == LOCAL,
            isPlayFromStart = domain.playItemsFromStart,
            isPinned = pinned,
            canPlay = domain.config.playable,
            canEdit = domain.config.editable,
            canDelete = domain.config.deletable,
            canEditItems = domain.config.editableItems,
            canDeleteItems = domain.config.deletableItems,
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
            itemsIdMap = itemsIdMap,
            canUpdate = domain.platformId != null && domain.platform == YOUTUBE
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
            title = stringDecoder.getString(StringResource.dialog_title_save_check),
            message = stringDecoder.getString(StringResource.dialog_message_save_item_check),
            confirm = AlertDialogModel.Button(StringResource.dialog_button_save, confirm),
            cancel = AlertDialogModel.Button(StringResource.dialog_button_dont_save, cancel),
        )

}
