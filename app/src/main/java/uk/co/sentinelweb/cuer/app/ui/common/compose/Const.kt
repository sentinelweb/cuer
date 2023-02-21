package uk.co.sentinelweb.cuer.app.ui.common.compose

import uk.co.sentinelweb.cuer.app.util.wrapper.log.AndroidLogWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.domain.NodeDomain.DeviceType.OTHER

object Const {
    val PREVIEW_BUILD_CONFIG = BuildConfigDomain(true, false, 0, "version", "device", OTHER)
    val PREVIEW_LOG_WRAPPER = AndroidLogWrapper(PREVIEW_BUILD_CONFIG)
}
