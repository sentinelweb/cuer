package uk.co.sentinelweb.cuer.app.db.repository.file

expect class AssetOperations {
    fun getAsString(path: String): String?
}