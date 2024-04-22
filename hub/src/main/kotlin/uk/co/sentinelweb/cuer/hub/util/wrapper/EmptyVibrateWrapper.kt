package uk.co.sentinelweb.cuer.hub.util.wrapper

import uk.co.sentinelweb.cuer.app.util.wrapper.VibrateWrapper

class EmptyVibrateWrapper : VibrateWrapper {
    override fun vibrate(time: Long) = Unit
}