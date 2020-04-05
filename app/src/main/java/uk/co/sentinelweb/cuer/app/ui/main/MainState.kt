package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextWrapper

class MainState constructor(
    // todo will need to disconnect/reconnect the view on config change
    var youtubePlayerContext: ChromecastYouTubePlayerContextWrapper? = null
) : ViewModel()

