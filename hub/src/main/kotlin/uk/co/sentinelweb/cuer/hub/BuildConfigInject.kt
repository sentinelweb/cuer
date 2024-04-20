package uk.co.sentinelweb.cuer.hub

object BuildConfigInject {
    val isDebug: Boolean = false.not()
    val cuerRemoteEnabled: Boolean = true
    val cuerBgPlayEnabled: Boolean = true
    val versionCode: Int = 1
    val version: String = "---"
}