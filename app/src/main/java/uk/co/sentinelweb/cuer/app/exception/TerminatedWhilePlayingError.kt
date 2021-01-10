package uk.co.sentinelweb.cuer.app.exception

import uk.co.sentinelweb.cuer.app.util.wrapper.ServiceWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class TerminatedWhilePlayingError(
    data: ServiceWrapper.Data,
    log: LogWrapper
) : Error("App terminated while playing") {
    init {
        log.w("Service data:" + data.toString())
    }
}