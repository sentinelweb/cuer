package uk.co.sentinelweb.cuer.app.db.repository.file

object ConfigDirectory {
    private val UserHome = "user.home"
    val Path = System.getProperty(UserHome) + "/.cuer"
}