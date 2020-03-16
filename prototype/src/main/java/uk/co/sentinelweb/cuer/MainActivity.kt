package uk.co.sentinelweb.cuer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.cast.framework.CastContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.utils.PlayServicesUtils
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.view_player_controls_example.*
import uk.co.sentinelweb.cuer.ui.queue.QueueFragment
import uk.co.sentinelweb.cuer.ui.queue.dummy.Queue
import uk.co.sentinelweb.cuer.ui.views.SimpleChromeCastUiController
import uk.co.sentinelweb.cuer.util.MediaRouteButtonUtils
import uk.co.sentinelweb.cuer.util.VideoIdProvider

class MainActivity : AppCompatActivity(), QueueFragment.OnListFragmentInteractionListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val googlePlayServicesAvailabilityRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            player_controls_view.isVisible = !player_controls_view.isVisible
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_queue, R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        // MediaRouteButtonUtils.initMediaRouteButton(media_route_button)// todo maybe have to go in player_controls_view
        MediaRouteButtonUtils.initMediaRouteButton(player_controls_view.findViewById(R.id.media_route_button) as androidx.mediarouter.app.MediaRouteButton)
// player_controls_view.findViewById(media_route_button as androidx.mediarouter.app.MediaRouteButton
        // can't use CastContext until I'm sure the user has GooglePlayServices
        PlayServicesUtils.checkGooglePlayServicesAvailability(this, googlePlayServicesAvailabilityRequestCode, Runnable { initChromeCast() })
    }

    private fun initChromeCast() {
        ChromecastYouTubePlayerContext(CastContext.getSharedInstance(this).sessionManager, SimpleChromeCastConnectionListener())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // can't use CastContext until I'm sure the user has GooglePlayServices
        if(requestCode == googlePlayServicesAvailabilityRequestCode)
            PlayServicesUtils.checkGooglePlayServicesAvailability(this, googlePlayServicesAvailabilityRequestCode, Runnable { initChromeCast() })
    }

    inner class SimpleChromeCastConnectionListener : ChromecastConnectionListener {

        private val chromeCastUiController = SimpleChromeCastUiController(player_controls_view)
        private val chromeCastConnectionStatusTextView = chromecast_controls_root.findViewById<TextView>(R.id.chromecast_connection_status)!!
        private val playerStatusTextView = chromecast_controls_root.findViewById<TextView>(R.id.player_status)!!

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
            chromecastYouTubePlayerContext.initialize(object: AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    chromeCastUiController.youTubePlayer = youTubePlayer

                    youTubePlayer.addListener(chromeCastUiController)
                    youTubePlayer.loadVideo(idProvider.getNextVideoId(), 0f)

                    chromecast_controls_root
                        .findViewById<Button>(R.id.next_video_button)
                        .setOnClickListener { youTubePlayer.loadVideo(idProvider.getNextVideoId(), 0f) }
                }

                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                    when(state) {
                        PlayerConstants.PlayerState.UNSTARTED -> playerStatusTextView.text = "UNSTARTED"
                        PlayerConstants.PlayerState.BUFFERING -> playerStatusTextView.text = "BUFFERING"
                        PlayerConstants.PlayerState.ENDED -> playerStatusTextView.text = "ENDED"
                        PlayerConstants.PlayerState.PAUSED -> playerStatusTextView.text = "PAUSED"
                        PlayerConstants.PlayerState.PLAYING -> playerStatusTextView.text = "PLAYING"
                        PlayerConstants.PlayerState.UNKNOWN -> playerStatusTextView.text = "UNKNOWN"
                        PlayerConstants.PlayerState.VIDEO_CUED -> playerStatusTextView.text = "VIDEO_CUED"
                        else -> Log.d(javaClass.simpleName, "unknown state")
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onListFragmentInteraction(item: Queue.QueueItem?) {
        Toast.makeText(this, "item:$item", Toast.LENGTH_SHORT).show()
    }


}
