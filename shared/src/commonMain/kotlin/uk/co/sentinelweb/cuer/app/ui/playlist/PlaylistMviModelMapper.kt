package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.AppPlaylistInteractor
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.mapper.IconMapper
import uk.co.sentinelweb.cuer.app.ui.common.resources.Icon
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringResource
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviContract.View.Header
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.APP
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistMviModelMapper constructor(
    private val itemModelMapper: PlaylistMviItemModelMapper,
    private val iconMapper: IconMapper,
    private val strings: StringDecoder,
    private val appPlaylistInteractors: Map<Identifier<GUID>, AppPlaylistInteractor>,
    private val util: PlaylistMviUtil,
    private val multiPlatformPreferences: MultiPlatformPreferencesWrapper,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun map(state: PlaylistMviContract.MviStore.State): PlaylistMviContract.View.Model =
        state.playlist?.let { playlist ->
            map(
                domain = playlist,
                isPlaying = util.isPlaylistPlaying(state),
                id = state.playlistIdentifier,
                playlists = state.playlistsTreeLookup,
                pinned = util.isPlaylistPinned(state),
                appPlaylist = state.playlist?.id?.let { appPlaylistInteractors[it] },
                itemsIdMapReversed = state.itemsIdMapReversed,
                blockItem = state.deletedPlaylistItem ?: state.movedPlaylistItem
            )
        } ?: DEFAULT_PLAYLIST_VIEW_MODEL

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        isMapItems: Boolean = true,
        id: Identifier<GUID>,
        pinned: Boolean,
        playlists: Map<Identifier<GUID>, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?,
        itemsIdMapReversed: MutableMap<PlaylistItemDomain, Identifier<GUID>>,
        blockItem: PlaylistItemDomain?
    ): PlaylistMviContract.View.Model {
        // log.d("map")
        val items = if (isMapItems) {
            mapItems(domain, itemsIdMapReversed, playlists, appPlaylist, blockItem)
        } else null
        return PlaylistMviContract.View.Model(
            header = Header(
                title = domain.title.capitalize(),
                imageUrl = (domain.image ?: domain.thumb)?.url
                    ?: throw IllegalArgumentException("No image for playlist"),
                loopModeIndex = domain.mode.ordinal,
                loopModeIcon = iconMapper.map(domain.mode),
                loopModeText = when (domain.mode) {
                    PlaylistDomain.PlaylistModeDomain.SINGLE -> strings.get(StringResource.menu_playlist_mode_single)
                    PlaylistDomain.PlaylistModeDomain.LOOP -> strings.get(StringResource.menu_playlist_mode_loop)
                    PlaylistDomain.PlaylistModeDomain.SHUFFLE -> strings.get(StringResource.menu_playlist_mode_shuffle)
                },
                loopVisible = domain.config.playable && domain.type != APP, // source == MEMORY ??
                playIcon = if (isPlaying) Icon.ic_playlist_close else Icon.ic_playlist_play,
                playText = strings.get(if (isPlaying) StringResource.stop else StringResource.menu_play),
                starredIcon = if (domain.starred) Icon.ic_starred else Icon.ic_starred_off,
                starredText = strings.get(if (domain.starred) StringResource.menu_unstar else StringResource.menu_star),
                isStarred = domain.starred,
                isDefault = domain.default,
                isSaved = id.source == LOCAL,
                isPlayFromStart = domain.playItemsFromStart,
                isPinned = pinned,
                canPlay = domain.config.playable,
                playEnabled = domain.items.size > 0,
                canEdit = domain.config.editable,
                canDelete = domain.config.deletable,
                canEditItems = domain.config.editableItems,
                canDeleteItems = domain.config.deletableItems,
                hasChildren = playlists?.get(domain.id)?.chidren?.size ?: 0,
                canUpdate = domain.platformId != null && domain.platform == YOUTUBE,
                shareVisible = true,
                shareEnabled = domain.items.isNotEmpty(),
                itemsText = domain.items
                    .takeIf { it.isNotEmpty() }
                    ?.let { "${domain.currentIndex.plus(1)}/${it.size}" }
                    ?: "-/-"
            ),
            items = items,
            isCards = multiPlatformPreferences.getBoolean(MultiPlatformPreferences.SHOW_VIDEO_CARDS, true),
            identifier = id,
            playingIndex = domain.currentIndex
        )
    }

    private fun mapItems(
        domain: PlaylistDomain,
        reverseLookup: MutableMap<PlaylistItemDomain, Identifier<GUID>>,
        playlists: Map<Identifier<GUID>, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?,
        blockItem: PlaylistItemDomain?
    ): List<PlaylistItemMviContract.Model.Item> {
        return domain.items
//            .also { log.d("state.items: ${it.size}") }
//            .also { log.d("guids: ${it.map { it.id }.joinToString(", ")}") }
            .filter { blockItem == null || blockItem.id == null || it.id != blockItem.id }
//            .also { log.d("state.items.filtered: ${it.size}, blockItem: $blockItem") }
            .mapIndexedNotNull { index, item ->
                reverseLookup.get(item)?.let { modelId ->
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
                } ?: run {
                    log.e("Couldn't get item: ${item.id} ${item.media.title}")
                    // fixme this might not be the best - should be in itemsIdMap
                    // throw IllegalStateException("Couldn't get item: ${item.id} ${item.media.title}")
                    null
                }
            }//.also { log.d("mapped.items: ${it.size}") }
    }

    fun mapPlaylistText(
        item: PlaylistItemDomain,
        domain: PlaylistDomain?,
        playlists: Map<Identifier<GUID>, PlaylistTreeDomain>?
    ): String? {
        val playlistText = item.playlistId?.let { itemPlaylistId ->
            if (itemPlaylistId != domain?.id) playlists?.get(itemPlaylistId)?.node?.title else null
        }
        return playlistText
    }

    fun mapSaveConfirmAlert(): AlertDialogModel =
        AlertDialogModel(
            title = strings.get(StringResource.dialog_title_save_check),
            message = strings.get(StringResource.dialog_message_save_item_check),
            confirm = AlertDialogModel.Button(StringResource.dialog_button_save) {},
            cancel = AlertDialogModel.Button(StringResource.dialog_button_dont_save) {},
        )

    companion object {
        val DEFAULT_PLAYLIST_VIEW_MODEL = PlaylistMviContract.View.Model(
            header = Header(
                title = "",
                imageUrl = null,
                loopModeIndex = 0,
                loopModeIcon = Icon.ic_playmode_straight,
                loopModeText = "Single",
                loopVisible = false,
                playIcon = Icon.ic_playlist_play,
                playText = "Play",
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
                hasChildren = 0,
                canUpdate = false,
                itemsText = "-/-",
                playEnabled = false,
                shareVisible = false,
                shareEnabled = false,
            ),
            items = null,
            isCards = false,
            identifier = null,
            playingIndex = null
        )
    }
}
