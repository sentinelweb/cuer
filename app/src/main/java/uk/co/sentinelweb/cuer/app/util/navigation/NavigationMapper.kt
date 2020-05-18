package uk.co.sentinelweb.cuer.app.util.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.LOCAL_PLAYER
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.Navigate.WEB_LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.LINK
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA_ID
import uk.co.sentinelweb.cuer.app.ui.ytplayer.YoutubeActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper

class NavigationMapper constructor(
    private val activity: Activity,
    private val toastWrapper: ToastWrapper
) {

    fun map(nav: NavigationModel) {
        when (nav.target) {
            LOCAL_PLAYER ->
                nav.params[MEDIA_ID]?.let {
                    YoutubeActivity.start(activity, it.toString())
                } ?: throw IllegalArgumentException("$LOCAL_PLAYER: $MEDIA_ID param required")
            WEB_LINK ->
                nav.params[LINK]?.let {
                    val parse = Uri.parse(it.toString())
                    activity.startActivity(
                        Intent.createChooser(
                            Intent(
                                Intent.ACTION_VIEW,
                                parse
                            ),
                            "Launch ${parse.host}"
                        )
                    )
                } ?: throw IllegalArgumentException("$WEB_LINK: $LINK param required")
            else -> toastWrapper.show("Cannot launch ${nav.target}")
        }
    }
}