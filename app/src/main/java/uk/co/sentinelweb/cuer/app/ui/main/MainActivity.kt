package uk.co.sentinelweb.cuer.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.ui.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

class MainActivity : AppCompatActivity(), MainContract.View {

    private val presenter: MainContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val snackBarWrapper: SnackbarWrapper by currentScope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.bottom_navigation))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        presenter.initialise()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_actionbar, menu)
        return true
    }

    override fun checkPlayServices() {
        // can't use CastContext until I'm sure the user has GooglePlayServices
        chromeCastWrapper.checkPlayServices(
            this,
            SERVICES_REQUEST_CODE,
            presenter::onPlayServicesOk
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // rerun check which definitely should pass here
        if (requestCode == SERVICES_REQUEST_CODE) {
            chromeCastWrapper.checkPlayServices(
                this,
                SERVICES_REQUEST_CODE,
                presenter::onPlayServicesOk
            )
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun isRecreating() = isChangingConfigurations

    override fun showMessage(msg: String) {
        snackBarWrapper.show(msg)
    }

    companion object {
        private val SERVICES_REQUEST_CODE = 1

        @JvmStatic
        val activityModule = module {
            scope(named<MainActivity>()) {
                scoped<MainContract.View> { getSource() }
                scoped<MainContract.Presenter> {
                    MainPresenter(
                        get(),
                        get(),
                        get(),
                        get(),
                        get(),
                        get()
                    )
                }
                scoped {
                    (getSource<MainActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.cast_player_fragment) as CastPlayerFragment).playerControls
                }
                viewModel { MainState() }
                scoped { SnackbarWrapper(getSource()) }
            }
        }
    }
}
