package uk.co.sentinelweb.cuer.app.ui.share

import androidx.annotation.DrawableRes
import uk.co.sentinelweb.cuer.domain.MediaDomain

data class ShareModel constructor(
    val isNewVideo: Boolean,
    val topRightButtonText: String?,
    @DrawableRes val topRightButtonIcon: Int,
    val topRightButtonAction: () -> Unit,
    val bottomRightButtonText: String?,
    @DrawableRes val bottomRightButtonIcon: Int,
    val bottomRightButtonAction: () -> Unit,
    val bottomLeftButtonText: String?,
    @DrawableRes val bottomLeftButtonIcon: Int,
    val topLeftButtonAction: () -> Unit,
    val topLeftButtonText: String?,
    @DrawableRes val topLeftButtonIcon: Int,
    val bottomLeftButtonAction: () -> Unit,
    val media: MediaDomain?
)