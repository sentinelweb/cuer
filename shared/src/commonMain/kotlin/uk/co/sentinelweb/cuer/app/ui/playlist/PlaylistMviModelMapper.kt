package uk.co.sentinelweb.cuer.app.ui.playlist

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
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
import uk.co.sentinelweb.cuer.domain.PlatformDomain.YOUTUBE
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.PlaylistTreeDomain

class PlaylistMviModelMapper constructor(
    private val itemModelMapper: PlaylistMviItemModelMapper,
    private val iconMapper: IconMapper,
    private val stringDecoder: StringDecoder,
    private val appPlaylistInteractors: Map<Long, AppPlaylistInteractor>,
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
                itemsIdMap = state.itemsIdMap
            )
        } ?: PlaylistMviContract.View.Model(
            header = Header(
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
                hasChildren = 0,
                canUpdate = false,
            ),
            items = listOf(),
            isCards = false,
        )

    fun map(
        domain: PlaylistDomain,
        isPlaying: Boolean,
        isMapItems: Boolean = true,
        id: OrchestratorContract.Identifier<*>,
        pinned: Boolean,
        playlists: Map<Long, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?,
        itemsIdMap: MutableMap<Long, PlaylistItemDomain>
    ): PlaylistMviContract.View.Model {

        val items = if (isMapItems) {
            mapItems(domain, itemsIdMap, playlists, appPlaylist)
        } else null
        return PlaylistMviContract.View.Model(
            header = Header(
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

                hasChildren = playlists?.get(domain.id)?.chidren?.size ?: 0,
                canUpdate = domain.platformId != null && domain.platform == YOUTUBE,
                /*&& !view.isHeadless*/ // fixme inject headless arg to mvi in bootstrap
            ),
            items = items,
            isCards = multiPlatformPreferences.getBoolean(MultiPlatformPreferences.SHOW_VIDEO_CARDS, true)
        )
    }

    private fun mapItems(
        domain: PlaylistDomain,
        itemsIdMap: MutableMap<Long, PlaylistItemDomain>,
        playlists: Map<Long, PlaylistTreeDomain>?,
        appPlaylist: AppPlaylistInteractor?
    ): List<PlaylistItemMviContract.Model.Item> {
        //log.d("items: ${itemsIdMap.size}")
        val reverseLookup: Map<PlaylistItemDomain, Long> = itemsIdMap.keys.associateBy { itemsIdMap[it]!! }
        //log.d("reverseLookup: ${reverseLookup.keys.map { "${it.id} -> ${reverseLookup[it]}" }.joinToString(":")}")
        return domain.items.mapIndexedNotNull { index, item ->
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
        }
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

    fun mapSaveConfirmAlert(): AlertDialogModel =
        AlertDialogModel(
            title = stringDecoder.getString(StringResource.dialog_title_save_check),
            message = stringDecoder.getString(StringResource.dialog_message_save_item_check),
            confirm = AlertDialogModel.Button(StringResource.dialog_button_save) {},
            cancel = AlertDialogModel.Button(StringResource.dialog_button_dont_save) {},
        )
}
