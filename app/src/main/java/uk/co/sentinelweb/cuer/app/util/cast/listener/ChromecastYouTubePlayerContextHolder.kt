package uk.co.sentinelweb.cuer.app.util.cast.listener

import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper

class ChromecastYouTubePlayerContextHolder constructor(
    private val creator: YoutubePlayerContextCreator,
    private val chromeCastWrapper: ChromeCastWrapper
) {
    private var _context: ChromecastYouTubePlayerContextWrapper? = null

    fun get(): ChromecastYouTubePlayerContextWrapper? = _context

    fun create() {
        _context = creator.createContext(chromeCastWrapper.getCastContext())
    }

    fun destroy() {
        _context?.apply {
            playerUi = null
            destroy()
        }
    }

    fun isCreated(): Boolean = _context != null
}