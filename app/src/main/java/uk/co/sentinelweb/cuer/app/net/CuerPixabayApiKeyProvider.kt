package uk.co.sentinelweb.cuer.app.net

import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.net.ApiKeyProvider

class CuerPixabayApiKeyProvider : ApiKeyProvider {
    override val key: String = BuildConfig.pixabayApiKey
}