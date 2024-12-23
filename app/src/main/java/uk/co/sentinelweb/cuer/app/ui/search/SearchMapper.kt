package uk.co.sentinelweb.cuer.app.ui.search

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Companion.PLAYLIST_SELECT_MODEL
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel.Type.PLAYLIST
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.domain.SearchTypeDomain
import uk.co.sentinelweb.cuer.domain.SearchTypeDomain.LOCAL
import uk.co.sentinelweb.cuer.domain.SearchTypeDomain.REMOTE

class SearchMapper constructor(
    private val res: ResourceWrapper,
    private val dateTimeFormatter: DateTimeFormatter
) {

    fun map(state: SearchContract.State): SearchContract.Model {
        val playlistQuantity = if (state.local.playlists.size > 0) "(${state.local.playlists.size})" else ""
        return SearchContract.Model(
            type = searchTypeText(state.searchType),
            icon = if (state.searchType == LOCAL) R.drawable.ic_portrait else R.drawable.ic_platform_youtube,
            otherType = searchTypeText(if (state.searchType == LOCAL) REMOTE else LOCAL),
            otherIcon = if (state.searchType == LOCAL) R.drawable.ic_platform_youtube else R.drawable.ic_portrait,
            text = if (state.searchType == LOCAL) state.local.text else state.remote.text ?: "",
            isLocal = state.searchType == LOCAL,
            localParams = state.local.let {
                SearchContract.LocalModel(
                    isWatched = it.isWatched,
                    isNew = it.isNew,
                    isLive = it.isLive,
                    playlists = listOf(PLAYLIST_SELECT_MODEL.copy(text = "Playlists $playlistQuantity ..."))
                        .plus(
                            state.local.playlists
                                .map { pl -> ChipModel(type = PLAYLIST, text = pl.title, value = pl.id?.toString()) }
                                .sortedBy { it.text.lowercase() }
                        )
                )
            },
            remoteParams = state.remote.let { remote ->
                SearchContract.RemoteModel(
                    platform = remote.platform,
                    relatedTo = remote.relatedToMediaPlatformId?.let { "${remote.relatedToMediaTitle} [$it]" },
                    channelPlatformId = remote.channelPlatformId,
                    isLive = remote.isLive,
                    fromDate = remote.fromDate?.let { dateTimeFormatter.formatDate(it.date) },
                    toDate = remote.toDate?.let { dateTimeFormatter.formatDate(it.date) },
                    order = remote.order
                )
            }
        )
    }

    fun searchTypeText(isLocal: Boolean) = searchTypeText(if (isLocal) LOCAL else REMOTE)

    private fun searchTypeText(type: SearchTypeDomain) = when (type) {
        LOCAL -> res.getString(R.string.search_local)
        REMOTE -> res.getString(R.string.search_youtube)
    }

}