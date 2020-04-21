package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextWrapper

class MainState constructor(
    var youtubePlayerContext: ChromecastYouTubePlayerContextWrapper? = null,
    var playServicesAvailable: Boolean = false,
    var playServiceCheckDone: Boolean = false
) : ViewModel()

