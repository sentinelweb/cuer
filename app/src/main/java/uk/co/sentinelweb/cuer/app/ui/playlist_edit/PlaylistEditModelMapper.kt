package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.channelUrl
import uk.co.sentinelweb.cuer.domain.platform.YoutubeUrl.Companion.playlistUrl

class PlaylistEditModelMapper constructor(
    private val res: ResourceWrapper,
    private val validator: PlaylistValidator,
) {
    fun mapModel(
        domain: PlaylistDomain,
        pinned: Boolean = false,
        parent: PlaylistDomain? = null,
        showAllWatched: Boolean,
        showDefault: Boolean,
        isDialog: Boolean
    ) =
        PlaylistEditContract.Model(
            titleDisplay = if (domain.title.isBlank()) res.getString(R.string.pe_default_display_title) else domain.title,
            titleEdit = domain.title,
            imageUrl = domain.image?.url,
            thumbUrl = domain.thumb?.url,
            starred = domain.starred,
            buttonText = res.getString(domain.id?.let { R.string.pe_save } ?: R.string.pe_create),
            pinned = pinned,
            playFromStart = domain.playItemsFromStart,
            default = domain.default,
            chip = parent?.let { ChipModel(ChipModel.Type.PLAYLIST, it.title, null, it.thumb) }
                ?: ChipModel.PLAYLIST_SELECT_MODEL,
            validation = validator.validate(domain),
            watchAllText = if (!showAllWatched) R.string.pe_mark_all_watched else R.string.pe_mark_all_unwatched,
            watchAllIIcon = if (!showAllWatched) R.drawable.ic_visibility_24 else R.drawable.ic_visibility_off_24,
            info = buildInfo(domain),
            config = domain.config,
            showDefault = showDefault,
            isDialog = isDialog
        )

    private fun buildInfo(domain: PlaylistDomain): String = "<b>Type</b>: ${domain.type}" +
            (if (domain.type == PLATFORM) "<br/><b>Platform</b>: ${domain.platform}" +
                    (domain.config.platformUrl?.let { "<br/><b>Platform URL</b>: ${domain.config.platformUrl}" }
                        ?: playlistUrl(domain))
            else "") +
            (domain.config.updateUrl?.let { "<br/><b>Update URL</b>: ${domain.config.updateUrl}" }
                ?: "") +
            (domain.config.description?.let { "<br/><br/><b>Description</b>:<br/> ${domain.config.description}" }
                ?: "") +
            (domain.channelData?.let {
                ("<br/><br/><b>Channel</b>: ${it.title}") +
                        ("<br/><b>Channel URL</b>: " + (it.customUrl
                            ?: channelUrl(it))) +
                        (it.description?.let { "<br/><br/><b>Description</b>:<br/> ${it}" } ?: "")
            } ?: "")

}