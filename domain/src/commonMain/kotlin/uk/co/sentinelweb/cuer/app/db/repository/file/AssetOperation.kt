package uk.co.sentinelweb.cuer.app.db.repository.file

expect class AssetOperation {
    fun getAsString(path: String): String?
}