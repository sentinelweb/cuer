package uk.co.sentinelweb.cuer.app.ui.search

import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST

class SearchMapper {

    fun map(state: SearchContract.State): SearchContract.Model = SearchContract.Model(
        state.text,
        state.isLocal,
        state.localParams.let {
            SearchContract.LocalModel(
                isWatched = it.isWatched,
                isNew = it.isNew,
                isLive = it.isLive,
                playlists = listOf(PLAYLIST_SELECT_MODEL)
                    .plus(state.localParams.playlists
                        .map { pl -> ChipModel(type = PLAYLIST, text = pl.title, value = pl.id?.toString()) }
                    )
            )
        },
        state.remoteParams.copy()
    )
}