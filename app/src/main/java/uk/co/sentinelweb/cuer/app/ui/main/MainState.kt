package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext

class MainState constructor(
    // todo will need to disconnect the view on config change
    var youtubePlayerContext: ChromecastYouTubePlayerContext? = null
) : ViewModel()

