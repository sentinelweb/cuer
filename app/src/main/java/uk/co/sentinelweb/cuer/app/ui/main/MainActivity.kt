package uk.co.sentinelweb.cuer.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.play_control.CastPlayerFragment
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistFragment
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.YoutubeJavaApiWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ext.deserialisePlaylistItem

class MainActivity :
    AppCompatActivity(),
    MainContract.View,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    NavigationProvider {

    private val presenter: MainContract.Presenter by currentScope.inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val snackBarWrapper: SnackbarWrapper by currentScope.inject()
    private val log: LogWrapper by currentScope.inject()
    private val navMapper: NavigationMapper by currentScope.inject()

    private lateinit var navController: NavController

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

//        navController = findNavController(R.id.nav_host_fragment)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        // setup nav draw https://developer.android.com/guide/navigation/navigation-ui#add_a_navigation_drawer
        bottom_nav_view.setupWithNavController(navController)
        intent.getStringExtra(Target.KEY) ?: run { navController.navigate(R.id.navigation_playlist) }
        presenter.initialise()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_actionbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.paste_add -> startActivity(ShareActivity.intent(this, true))
            //R.id.restart_conn -> presenter.restartYtCastContext()
            R.id.settings -> navController.navigate(R.id.navigation_settings_root)
        }
        return super.onOptionsItemSelected(item)
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
        if (requestCode == SERVICES_REQUEST_CODE) {
            chromeCastWrapper.checkPlayServices(
                this,
                SERVICES_REQUEST_CODE,
                presenter::onPlayServicesOk
            )
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkForPendingNavigation(null)?.apply { navMapper.map(this) }
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
        //checkIntent(intent)
        checkForPendingNavigation(null)?.apply { navMapper.map(this) }
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
        snackBarWrapper.make(msg)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        when (pref.title) {
            getString(R.string.prefs_root_backup_item_title) -> navController.navigate(R.id.navigation_settings_backup)
        }
        return true
    }

    override fun navigate(destination: NavigationModel) {
        navMapper.map(destination)
    }

    override fun checkForPendingNavigation(target: Target?): NavigationModel? {
        log.d("checkForPendingNavigation:$target > ${intent.getStringExtra(Target.KEY)}")
        return intent.getStringExtra(Target.KEY)
            ?.takeIf { target == null || it == target.name }
            ?.let {
                return when (it) {
                    PLAYLIST_FRAGMENT.name ->
                        PLAYLIST_ITEM.getString(intent)
                            ?.let { deserialisePlaylistItem(it) }
                            ?.let { item ->
                                PlaylistFragment.makeNav(item, PLAY_NOW.getBoolean(intent)).apply {
                                    log.d("got nav:$this")
                                }
                            }

                    else -> null
                }
            }
    }

    override fun clearPendingNavigation(target: Target) {
        navMapper.clearArgs(intent, target)
    }

    companion object {
        val TOP_LEVEL_DESTINATIONS =
            setOf(R.id.navigation_browse, R.id.navigation_playlists, R.id.navigation_playlist, R.id.navigation_player)
        private const val SERVICES_REQUEST_CODE = 1

        @JvmStatic
        val activityModule = module {
            scope(named<MainActivity>()) {
                scoped<MainContract.View> { getSource() }
                scoped<MainContract.Presenter> {
                    MainPresenter(
                        view = get(),
                        state = get(),
                        playerControls = get(),
                        ytServiceManager = get(),
                        ytContextHolder = get(),
                        log = get()
                    )
                }
                scoped {
                    (getSource<MainActivity>()
                        .supportFragmentManager
                        .findFragmentById(R.id.cast_player_fragment) as CastPlayerFragment).playerControls
                }
                scoped {
                    NavigationMapper(
                        activity = getSource(),
                        toastWrapper = get(),
                        ytJavaApi = get(),
                        navController = (getSource<AppCompatActivity>()
                            .supportFragmentManager
                            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                            .navController,
                        log = get()
                    )
                }
                scoped { YoutubeJavaApiWrapper(getSource()) }
                viewModel { MainState() }
                scoped { SnackbarWrapper(getSource()) }
            }
        }
    }
}
