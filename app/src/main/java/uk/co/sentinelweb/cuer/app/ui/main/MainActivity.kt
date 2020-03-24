package uk.co.sentinelweb.cuer.app.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.cast.framework.CastContext
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.view_player_controls_example.*
import kotlinx.android.synthetic.main.view_player_controls_example.view.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.SimpleChromeCastConnectionListener
import uk.co.sentinelweb.cuer.app.util.cast.SimpleChromeCastUiController

class MainActivity : AppCompatActivity(), MainContract.View {

    private val presenter: MainContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_browse,
                R.id.navigation_playlist,
                R.id.navigation_player
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        presenter.initChromecast()
    }

    override fun initMediaRouteButton() {
        chromeCastWrapper.initMediaRouteButton(player_controls_view.media_route_button)
    }

    override fun checkPlayServices() {
        // can't use CastContext until I'm sure the user has GooglePlayServices
        chromeCastWrapper.checkPlayServices(this,SERVICES_REQUEST_CODE, this::initChromeCast )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // rerun check which definitely should pass here
        if (requestCode == SERVICES_REQUEST_CODE) {
            chromeCastWrapper.checkPlayServices(this,SERVICES_REQUEST_CODE, this::initChromeCast )
        }
    }

    private fun initChromeCast() {
        ChromecastYouTubePlayerContext(
            CastContext.getSharedInstance(this).sessionManager, SimpleChromeCastConnectionListener(
                SimpleChromeCastUiController(player_controls_view),
                chromecast_connection_status,
                player_status,
                chromecast_controls_root
            )
        )
    }

    companion object {
        private val SERVICES_REQUEST_CODE = 1
        @JvmStatic
        val activityModule = module {
            scope(named<MainActivity>()) {
                scoped<MainContract.View> { getSource() }
                scoped<MainContract.Presenter> { MainPresenter(get(), get()) }
                viewModel { MainState() }
            }
        }
    }
}
