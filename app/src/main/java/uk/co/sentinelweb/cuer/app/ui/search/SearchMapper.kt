package uk.co.sentinelweb.cuer.app.ui.search

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST

class SearchMapper {

    fun map(state: SearchContract.State): SearchContract.Model {
        val playlistQuantity = if (state.search.localParams.playlists.size > 0) "(${state.search.localParams.playlists.size})" else ""
        return SearchContract.Model(
            if (state.search.isLocal) state.search.localParams.text else state.search.remoteParams.text,
            state.search.isLocal,
            state.search.localParams.let {
                SearchContract.LocalModel(
                    isWatched = it.isWatched,
                    isNew = it.isNew,
                    isLive = it.isLive,
                    playlists = listOf(PLAYLIST_SELECT_MODEL.copy(text = "Playlists $playlistQuantity ..."))
                        .plus(state.search.localParams.playlists
                            .map { pl -> ChipModel(type = PLAYLIST, text = pl.title, value = pl.id?.toString()) }
                            .sortedBy { it.text.toLowerCase() }
                        )
                )
            },
            state.search.remoteParams.let {
                SearchContract.RemoteModel(
                    platform = it.platform,
                    relatedToPlatformId = it.relatedToPlatformId,
                    channelPlatformId = it.channelPlatformId,
                    isLive = it.isLive
                )
            }
        )
    }
}