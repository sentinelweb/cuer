package uk.co.sentinelweb.cuer.app.db.init

import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.ImageDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface DatabaseInitializer {
    fun isInitialized(): Boolean
    fun initDatabase(path: String)
    fun addListener(
        listener: (Boolean) -> Unit,
    )

    companion object {
        val DEFAULT_PLAYLIST_ID = OrchestratorContract.Identifier<GUID>(GUID("8bbbdf48-83d5-49cb-a29a-f0e3e67dd207"), LOCAL)
        val PHILOSOPHY_PLAYLIST_ID = OrchestratorContract.Identifier<GUID>(GUID("2e479e7f-d09b-4c52-8f32-176a15b6dfc5"), LOCAL)
        val DEFAULT_PLAYLIST_TEMPLATE = PlaylistDomain(
            title = "Default",
            image = ImageDomain(url = "gs://cuer-275020.appspot.com/playlist_header/pexels-freestocksorg-34407-600.jpg"),
            default = true
        )
    }
}