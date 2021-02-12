package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA

class ShareModelMapper constructor(
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val res: ResourceWrapper
) {
    fun mapShareModel(
        scanResult: ScanContract.Result,
        finish: (Boolean, Boolean, Boolean) -> Unit
    ): ShareContract.Model {
        val isConnected = ytContextHolder.isConnected()
        val isNew = scanResult.isNew || (scanResult.type == MEDIA && !scanResult.isOnPlaylist)
        return if (isNew) {
            ShareContract.Model(
                isNew = isNew,
                topRight = ShareContract.Model.Button(
                    action = { finish(/*add = */true, /*play = */ true, /*forward = */ true) },
                    text = if (isConnected) res.getString(R.string.share_button_play_now) else null,
                    icon = if (isConnected)
                        R.drawable.ic_notif_status_cast_conn_white
                    else R.drawable.ic_button_play_black,
                ),
                topLeft = ShareContract.Model.Button(
                    action = { finish(/*add = */true, /*play = */ true, /*forward = */ false) },
                    text = if (isConnected) res.getString(R.string.share_button_play_return) else null,
                    icon = if (isConnected)
                        R.drawable.ic_notif_status_cast_conn_white
                    else R.drawable.ic_button_play_black
                ),
                bottomRight = ShareContract.Model.Button(
                    action = { finish(/*add = */true, /*play = */ false, /*forward = */ true) },
                    text = res.getString(R.string.share_button_goto_item),
                    icon = R.drawable.ic_button_forward_black
                ),
                bottomLeft = ShareContract.Model.Button(
                    action = { finish(/*add = */true, /*play = */ false, /*forward = */ false) },
                    text = res.getString(R.string.share_button_return),
                    icon = R.drawable.ic_button_back_black
                )
            )
        } else {
            ShareContract.Model(
                isNew = isNew,
                topRight = ShareContract.Model.Button(
                    action = { finish(/*add = */false, /*play = */ true, /*forward = */ true) },
                    text = if (isConnected)
                        res.getString(R.string.share_button_play_now)
                    else null,
                    icon = if (isConnected)
                        R.drawable.ic_notif_status_cast_conn_white
                    else R.drawable.ic_button_play_black
                ),
                topLeft = ShareContract.Model.Button(
                    action = { finish(/*add = */false, /*play = */ true, /*forward = */ false) },
                    text = if (isConnected)
                        res.getString(R.string.share_button_play_return)
                    else null,
                    icon = if (isConnected)
                        R.drawable.ic_notif_status_cast_conn_white
                    else R.drawable.ic_button_play_black
                ),
                bottomRight = ShareContract.Model.Button(
                    action = { finish(/*add = */false, /*play = */ false, /*forward = */ true) },
                    text = res.getString(R.string.share_button_goto_item),
                    icon = R.drawable.ic_button_forward_black
                ),
                bottomLeft = ShareContract.Model.Button(
                    action = { finish(/*add = */false, /*play = */ false, /*forward = */ false) },
                    text = res.getString(R.string.share_button_return),
                    icon = R.drawable.ic_button_back_black
                )
            )
        }
    }

    fun mapEmptyState(finish: (Boolean, Boolean, Boolean) -> Unit) =
        ShareContract.Model(
            topRight = ShareContract.Model.Button(),
            topLeft = ShareContract.Model.Button(),
            bottomRight = ShareContract.Model.Button(
                action = { finish(/*add = */false, /*play = */ false, /*forward = */ true) },
                text = res.getString(R.string.share_button_goto_app),
                icon = R.drawable.ic_button_forward_black
            ),
            bottomLeft = ShareContract.Model.Button(
                action = { finish(/*add = */false, /*play = */ false, /*forward = */ false) },
                text = res.getString(R.string.share_button_return),
                icon = R.drawable.ic_button_back_black
            ),
            isNew = false
        )
}