package uk.co.sentinelweb.cuer.app.util.cast

import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue

class SimpleChromeCastConnectionListener constructor(
    private val chromeCastUiController: SimpleChromeCastUiController,
    private val chromeCastConnectionStatusTextView: TextView,
    private val playerStatusTextView: TextView,
    private val chromecast_controls_root: ViewGroup
) : ChromecastConnectionListener {

    override fun onChromecastConnecting() {
        Log.d(javaClass.simpleName, "onChromecastConnecting")
        chromeCastConnectionStatusTextView.text = "connecting to chromecast..."
        chromeCastUiController.showProgressBar()
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        Log.d(javaClass.simpleName, "onChromecastConnected")
        chromeCastConnectionStatusTextView.text = "connected to chromecast"

        initializeCastPlayer(chromecastYouTubePlayerContext)
    }

    override fun onChromecastDisconnected() {
        Log.d(javaClass.simpleName, "onChromecastDisconnected")
        chromeCastConnectionStatusTextView.text = "not connected to chromecast"
        chromeCastUiController.resetUi()
        playerStatusTextView.text = ""
    }

    private fun initializeCastPlayer(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        val idProvider = Queue.VideoIdProvider()
        chromecastYouTubePlayerContext.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                chromeCastUiController.youTubePlayer = youTubePlayer

                youTubePlayer.addListener(chromeCastUiController)
                youTubePlayer.loadVideo(idProvider.getNextVideoId(), 0f)

                chromecast_controls_root
                    .findViewById<Button>(R.id.next_video_button)
                    .setOnClickListener { youTubePlayer.loadVideo(idProvider.getNextVideoId(), 0f) }
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                when (state) {
                    UNSTARTED -> playerStatusTextView.text = "UNSTARTED"
                    BUFFERING -> playerStatusTextView.text = "BUFFERING"
                    ENDED -> playerStatusTextView.text = "ENDED"
                    PAUSED -> playerStatusTextView.text = "PAUSED"
                    PLAYING -> playerStatusTextView.text = "PLAYING"
                    UNKNOWN -> playerStatusTextView.text = "UNKNOWN"
                    VIDEO_CUED -> playerStatusTextView.text = "VIDEO_CUED"
                    else -> Log.d(javaClass.simpleName, "unknown state")
                }
            }
        })
    }
}