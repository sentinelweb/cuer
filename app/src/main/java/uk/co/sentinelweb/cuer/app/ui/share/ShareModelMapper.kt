package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class ShareModelMapper constructor(
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val res: ResourceWrapper
) {
    fun mapShareModel(
        media: MediaDomain?,
        playlistItems: List<PlaylistItemDomain>?,
        finish: (Boolean, Boolean, Boolean) -> Unit
    ): ShareModel {
        val isConnected = ytContextHolder.isConnected()
        val isNew = media?.id == null || playlistItems?.size ?: 0 == 0
        return if (isNew) {
            ShareModel(
                topRightButtonAction = {
                    finish(/*add = */true, /*play = */ true, /*forward = */ true)
                },
                topRightButtonText = if (isConnected)
                    res.getString(R.string.share_button_play_now)
                else res.getString(
                    R.string.share_button_play_locally
                ),
                topRightButtonIcon = if (isConnected)
                    R.drawable.ic_notif_status_cast_conn_white
                else R.drawable.ic_button_play_black,
                topLeftButtonAction = {
                    finish(/*add = */true, /*play = */ true, /*forward = */ false)
                },
                topLeftButtonText = if (isConnected)
                    res.getString(R.string.share_button_play_return)
                else null,
                topLeftButtonIcon = if (isConnected)
                    R.drawable.ic_notif_status_cast_conn_white
                else R.drawable.ic_button_play_black,
                bottomRightButtonAction = {
                    finish(/*add = */true, /*play = */ false, /*forward = */ true)
                },
                bottomRightButtonText = res.getString(R.string.share_button_add_to_queue),
                bottomRightButtonIcon = R.drawable.ic_button_add_black,
                bottomLeftButtonAction = {
                    finish(/*add = */true, /*play = */ false, /*forward = */ false)
                },
                bottomLeftButtonText = res.getString(R.string.share_button_add_return),
                bottomLeftButtonIcon = R.drawable.ic_button_add_black,
                media = media,
                isNewVideo = isNew
            )
        } else {
            ShareModel(
                topRightButtonAction = {
                    finish(/*add = */false, /*play = */ true, /*forward = */ true)
                },
                topRightButtonText = if (isConnected)
                    res.getString(R.string.share_button_play_now)
                else res.getString(R.string.share_button_play_locally),
                topRightButtonIcon = if (isConnected)
                    R.drawable.ic_notif_status_cast_conn_white
                else R.drawable.ic_button_play_black,
                topLeftButtonAction = {
                    finish(/*add = */false, /*play = */ true, /*forward = */ false)
                },
                topLeftButtonText = if (isConnected)
                    res.getString(R.string.share_button_play_return)
                else null,
                topLeftButtonIcon = if (isConnected)
                    R.drawable.ic_notif_status_cast_conn_white
                else R.drawable.ic_button_play_black,
                bottomRightButtonAction = {
                    finish(/*add = */false, /*play = */ false, /*forward = */ true)
                },
                bottomRightButtonText = res.getString(R.string.share_button_goto_item),
                bottomRightButtonIcon = R.drawable.ic_button_forward_black,
                bottomLeftButtonAction = {
                    finish(/*add = */false, /*play = */ false, /*forward = */ false)
                },
                bottomLeftButtonText = res.getString(R.string.share_button_return),
                bottomLeftButtonIcon = R.drawable.ic_button_back_black,
                media = media,
                isNewVideo = isNew
            )
        }
    }

    fun mapEmptyState(finish: (Boolean, Boolean, Boolean) -> Unit) =
        ShareModel(
            topRightButtonAction = {},
            topRightButtonText = null,
            topRightButtonIcon = 0,
            topLeftButtonAction = { },
            topLeftButtonText = null,
            topLeftButtonIcon = 0,
            bottomRightButtonAction = {
                finish(/*add = */false, /*play = */ false, /*forward = */ true)
            },
            bottomRightButtonText = res.getString(R.string.share_button_goto_app),
            bottomRightButtonIcon = R.drawable.ic_button_forward_black,
            bottomLeftButtonAction = {
                finish(/*add = */false, /*play = */ false, /*forward = */ false)
            },
            bottomLeftButtonText = res.getString(R.string.share_button_return),
            bottomLeftButtonIcon = R.drawable.ic_button_back_black,
            media = null,
            isNewVideo = false
        )
}