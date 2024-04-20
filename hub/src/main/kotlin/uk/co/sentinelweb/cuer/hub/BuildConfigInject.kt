package uk.co.sentinelweb.cuer.hub

object BuildConfigInject {
    val isDebug: Boolean = false.not()
    val cuerRemoteEnabled: Boolean = true
    val cuerBgPlayEnabled: Boolean = true
    val versionCode: Int = 12
    val version: String = "0.79"
}