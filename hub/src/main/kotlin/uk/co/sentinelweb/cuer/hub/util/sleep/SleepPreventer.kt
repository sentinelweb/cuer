package uk.co.sentinelweb.cuer.hub.util.sleep

interface SleepPreventer {
    fun preventSleep()

    fun allowSleep()
}