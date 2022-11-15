package uk.co.sentinelweb.cuer.app.db.init

import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface DatabaseInitializer {
    fun isInitialized(): Boolean
    fun initDatabase(path: String)
    fun addListener(
        listener: (Boolean) -> Unit,
    )

    companion object {
        val DEFAULT_PLAYLIST_TEMPLATE = PlaylistDomain(
            title = "Default",
            image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-freestocksorg-34407-600.jpg"),
            default = true
        )
    }
}