package uk.co.sentinelweb.cuer.app.ui.search

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract.SearchType.LOCAL
import uk.co.sentinelweb.cuer.app.ui.search.SearchContract.SearchType.REMOTE
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter

class SearchMapper constructor(
    private val res: ResourceWrapper,
    private val dateTimeFormatter: DateTimeFormatter
) {

    fun map(state: SearchContract.State): SearchContract.Model {
        val playlistQuantity = if (state.local.playlists.size > 0) "(${state.local.playlists.size})" else ""
        return SearchContract.Model(
            searchTypeText(state.searchType),
            searchTypeText(if (state.searchType == LOCAL) REMOTE else LOCAL),
            if (state.searchType == LOCAL) state.local.text else state.remote.text ?: "",
            state.searchType == LOCAL,
            state.local.let {
                SearchContract.LocalModel(
                    isWatched = it.isWatched,
                    isNew = it.isNew,
                    isLive = it.isLive,
                    playlists = listOf(PLAYLIST_SELECT_MODEL.copy(text = "Playlists $playlistQuantity ..."))
                        .plus(state.local.playlists
                            .map { pl -> ChipModel(type = PLAYLIST, text = pl.title, value = pl.id?.toString()) }
                            .sortedBy { it.text.toLowerCase() }
                        )
                )
            },
            state.remote.let {
                SearchContract.RemoteModel(
                    platform = it.platform,
                    relatedTo = it.run { "$relatedToMediaTitle [$relatedToMediaPlatformId]" },
                    channelPlatformId = it.channelPlatformId,
                    isLive = it.isLive,
                    fromDate = it.fromDate?.let { dateTimeFormatter.formatDate(it.toLocalDate()) },
                    toDate = it.toDate?.let { dateTimeFormatter.formatDate(it.toLocalDate()) },
                    order = it.order
                )
            }
        )
    }

    fun searchTypeText(isLocal: Boolean) = searchTypeText(if (isLocal) LOCAL else REMOTE)

    private fun searchTypeText(type: SearchContract.SearchType) = when (type) {
        LOCAL -> res.getString(R.string.search_local)
        REMOTE -> res.getString(R.string.search_youtube)
    }

}