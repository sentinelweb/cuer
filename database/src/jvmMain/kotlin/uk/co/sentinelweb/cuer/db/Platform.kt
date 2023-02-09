package uk.co.sentinelweb.cuer.db

actual class Platform actual constructor() {
    actual val platform: String = "JVM ${Runtime.version()}"
}