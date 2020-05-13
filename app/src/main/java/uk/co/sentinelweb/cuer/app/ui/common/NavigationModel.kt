package uk.co.sentinelweb.cuer.app.ui.common

import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.*

data class NavigationModel constructor(
    val target: Navigate,
    val params: Map<NavigateParam, Any> = mapOf()
) {
    init {
        params.keys
            .containsAll(target.requiredParams)
            .takeUnless { true }
            ?: throw IllegalArgumentException("$target requires ${target.requiredParams}")
    }

    enum class Navigate constructor(
        val requiredParams: List<NavigateParam>
    ) {
        LOCAL_PLAYER(listOf(MEDIA_ID)),
        WEB_LINK((listOf(LINK))),
        YOUTUBE_VIDEO((listOf(MEDIA_ID))), // todo map
        YOUTUBE_CHANNEL((listOf(CHANNEL_ID))), // todo map
        MAIN_MEDIA((listOf(MEDIA))), // todo map
        MAIN_MEDIA_PLAY((listOf(MEDIA, PLAY_NOW))), // todo map
        SHARE((listOf(MEDIA))) // todo map
    }

    enum class NavigateParam {
        MEDIA_ID, /* String */
        CHANNEL_ID, /* String */
        MEDIA, /* MediaDomain */
        LINK, /* String */
        PLAY_NOW /* Boolean */
    }
}