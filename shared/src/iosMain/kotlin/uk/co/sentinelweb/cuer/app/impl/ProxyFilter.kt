package uk.co.sentinelweb.cuer.app.impl

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Filter
import uk.co.sentinelweb.cuer.domain.PlatformDomain

class ProxyFilter {
    fun allFilter() = Filter.AllFilter
    fun defaultFilter() = Filter.DefaultFilter
    fun idListFilter(list: List<Long>) =
        Filter.IdListFilter(list)

    fun mediaListFilter(ids: List<Long>) = Filter.MediaIdListFilter(ids)
    fun platformIdListFilter(ids: List<String>, platform: PlatformDomain = PlatformDomain.YOUTUBE) =
        Filter.PlatformIdListFilter(ids, platform)

    fun channelPlatformIdFilter(platformId: String) = Filter.ChannelPlatformIdFilter(platformId)
    fun newMediaFilter(limit: Int) = Filter.NewMediaFilter(limit)
    fun recentMediaFilter(limit: Int) = Filter.RecentMediaFilter(limit)
    fun starredMediaFilter(limit: Int) = Filter.StarredMediaFilter(limit)
    fun unfinishedMediaFilter(minPercent: Int, maxPercent: Int, limit: Int) =
        Filter.UnfinishedMediaFilter(minPercent, maxPercent, limit)

    fun titleFilter(title: String) = Filter.TitleFilter(title)
    fun searchFilter(text: String, isWatched: Boolean, isNew: Boolean, isLive: Boolean, playlistIds: List<Long>?) =
        Filter.SearchFilter(text, isWatched, isNew, isLive, playlistIds)

    fun playlistIdLFilter(id: Long) = Filter.PlaylistIdLFilter(id)
}