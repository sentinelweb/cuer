package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class ShareModelMapper constructor(
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val res: ResourceWrapper
) {
    fun mapShareModel(
        scanResult: ScanContract.Result,
        finish: (Boolean, Boolean, Boolean) -> Unit
    ): ShareContract.Model {
        val isConnected = ytContextHolder.isConnected()
        val isNew = scanResult.isNew || !scanResult.isOnPlaylist
        return if (isNew) {
            ShareContract.Model(
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
                isNew = isNew
            )
        } else {
            ShareContract.Model(
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
                isNew = isNew
            )
        }
    }

    fun mapEmptyState(finish: (Boolean, Boolean, Boolean) -> Unit) =
        ShareContract.Model(
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
            isNew = false
        )
}