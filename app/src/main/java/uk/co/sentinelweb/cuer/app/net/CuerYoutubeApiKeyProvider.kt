package uk.co.sentinelweb.cuer.app.net

import uk.co.sentinelweb.cuer.app.BuildConfig
import uk.co.sentinelweb.cuer.net.ApiKeyProvider

class CuerYoutubeApiKeyProvider : ApiKeyProvider {
    override val key: String = BuildConfig.youtubeApiKey
}