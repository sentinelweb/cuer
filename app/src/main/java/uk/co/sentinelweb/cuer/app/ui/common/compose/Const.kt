package uk.co.sentinelweb.cuer.app.ui.common.compose

import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain

object Const {
    val PREVIEW_BUILD_CONFIG = BuildConfigDomain(true, false, 0, "")
    val PREVIEW_LOG_WRAPPER = AndroidLogWrapper(PREVIEW_BUILD_CONFIG)
}
