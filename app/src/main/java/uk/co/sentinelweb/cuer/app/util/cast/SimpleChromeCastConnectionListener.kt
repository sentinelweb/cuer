package uk.co.sentinelweb.cuer.app.util.cast

import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
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
        val idProvider = VideoIdProvider()
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
                    PlayerConstants.PlayerState.UNSTARTED -> playerStatusTextView.text = "UNSTARTED"
                    PlayerConstants.PlayerState.BUFFERING -> playerStatusTextView.text = "BUFFERING"
                    PlayerConstants.PlayerState.ENDED -> playerStatusTextView.text = "ENDED"
                    PlayerConstants.PlayerState.PAUSED -> playerStatusTextView.text = "PAUSED"
                    PlayerConstants.PlayerState.PLAYING -> playerStatusTextView.text = "PLAYING"
                    PlayerConstants.PlayerState.UNKNOWN -> playerStatusTextView.text = "UNKNOWN"
                    PlayerConstants.PlayerState.VIDEO_CUED -> playerStatusTextView.text =
                        "VIDEO_CUED"
                    else -> Log.d(javaClass.simpleName, "unknown state")
                }
            }
        })
    }

    class VideoIdProvider () {
        private var index = 0;

        fun getNextVideoId() = getId(Queue.ITEMS[index].url).also{index ++}

        fun getId(url:String) = url.substring(url.indexOf("?v=")+3)
    }
}