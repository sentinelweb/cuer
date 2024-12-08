package uk.co.sentinelweb.cuer.hub.util.sleep

class SleepPreventerEmpty : SleepPreventer {
    override fun preventSleep() = Unit

    override fun allowSleep() = Unit
}