package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.chip.ChipModel
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.channelUrl
import uk.co.sentinelweb.cuer.app.util.link.YoutubeUrl.Companion.playlistUrl
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistTypeDomain.PLATFORM

class PlaylistEditModelMapper constructor(
    private val res: ResourceWrapper,
    private val validator: PlaylistValidator,
) {
    fun mapModel(
        state: PlaylistEditContract.State,
        pinned: Boolean = false,
    ) = with(state.playlistEdit) {
        val showAllWatched = state.isAllWatched == true
        PlaylistEditContract.Model(
            titleDisplay = if (title.isBlank()) res.getString(R.string.pe_default_display_title) else title,
            titleEdit = title,
            imageUrl = image?.url,
            thumbUrl = thumb?.url,
            starred = starred,
            buttonText = res.getString(id?.let { R.string.pe_save } ?: R.string.pe_create),
            pinned = pinned,
            playFromStart = playItemsFromStart,
            default = default,
            chip = state.playlistParent?.let { ChipModel(ChipModel.Type.PLAYLIST, it.title, null, it.thumb) }
                ?: ChipModel.PLAYLIST_SELECT_MODEL,
            validation = validator.validate(this),
            watchAllText = if (!showAllWatched) R.string.pe_mark_all_watched else R.string.pe_mark_all_unwatched,
            watchAllIIcon = if (!showAllWatched) R.drawable.ic_visibility else R.drawable.ic_visibility_off,
            info = buildInfo(this),
            config = config,
            showDefault = !state.defaultInitial,
            isDialog = state.isDialog,
            isCreate = state.isCreate
        )
    }

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