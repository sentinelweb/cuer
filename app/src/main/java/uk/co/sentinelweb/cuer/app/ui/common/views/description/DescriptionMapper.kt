package uk.co.sentinelweb.cuer.app.ui.common.views.description

import kotlinx.datetime.toJavaLocalDateTime
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract.DescriptionModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.core.mappers.DateTimeFormatter
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class DescriptionMapper constructor(
    private val dateTimeFormater: DateTimeFormatter,
    private val res: ResourceWrapper
) {
    fun map(domain: MediaDomain, selectedPlaylists: Set<PlaylistDomain>): DescriptionModel =
        DescriptionModel(
            title = domain.title,
            description = domain.description,

            channelTitle = domain.channelData.title,
            channelThumbUrl = (domain.channelData.thumbNail ?: domain.channelData.image)?.url,
            channelDescription = domain.channelData.description,
            chips = mutableListOf(ChipModel.PLAYLIST_SELECT_MODEL).apply {
                selectedPlaylists.forEachIndexed { index, playlist ->
                    add(index, ChipModel(ChipModel.Type.PLAYLIST, playlist.title, playlist.id.toString(), playlist.thumb ?: playlist.image))
                }
            },
            pubDate = dateTimeFormater.formatDateTimeNullable(domain.published?.toJavaLocalDateTime()),
        )

    fun mapEmpty() = DescriptionModel(
        title = res.getString(R.string.pie_empty_title),
        description = res.getString(R.string.pie_empty_desc),
        pubDate = null,
        chips = listOf(),
        channelTitle = null,
        channelThumbUrl = null,
        channelDescription = null,
    )

}