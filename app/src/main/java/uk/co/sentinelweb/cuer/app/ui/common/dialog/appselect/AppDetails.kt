package uk.co.sentinelweb.cuer.app.ui.common.dialog.appselect

import android.graphics.drawable.Drawable
import uk.co.sentinelweb.cuer.domain.AppDetailsDomain

data class AppDetails(
    override val title: CharSequence,
    override val appId: String,
    val icon: Drawable
) : AppDetailsDomain(title, appId)