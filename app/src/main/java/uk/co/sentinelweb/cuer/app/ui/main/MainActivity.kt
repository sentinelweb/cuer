package uk.co.sentinelweb.cuer.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.MainActivityBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistContract
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.LAST_BOTTOM_TAB
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class MainActivity :
    AppCompatActivity(),
    MainContract.View,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    NavigationProvider,
    PlaylistItemEditContract.DoneNavigation,
    AndroidScopeComponent,
    MainContract.PlayerViewControl {

    override val scope: Scope by activityScopeWithSource()

    private val presenter: MainContract.Presenter by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val snackBarWrapper: SnackbarWrapper by inject()
    private val log: LogWrapper by inject()
    private val navMapper: NavigationMapper by inject()
    private val volumeControl: CuerSimpleVolumeController by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val res: ResourceWrapper by inject()
    private val prefs: GeneralPreferencesWrapper by inject()
    private lateinit var navController: NavController

    private var _binding: MainActivityBinding? = null
    private val binding: MainActivityBinding
        get() = _binding ?: throw Exception("Main view not bound")
    private val playerFragment: Fragment by lazy {
        supportFragmentManager.findFragmentById(R.id.cast_player_fragment)
            ?: throw Exception("No player fragment")
    }

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)
        binding.bottomNavView.setOnNavigationItemSelectedListener {
            val value = when (it.itemId) {
                R.id.navigation_browse -> 0
                R.id.navigation_playlists -> 1
                R.id.navigation_playlist -> 2
                else -> 0
            }
            prefs.putInt(LAST_BOTTOM_TAB, value)
            navController.navigate(it.itemId)
            true
        }

        edgeToEdgeWrapper.doOnApplyWindowInsets(binding.bottomNavView) { view, insets, padding ->
            view.updatePadding(
                bottom = padding.bottom + insets.systemWindowInsetBottom
            )
        }

        prefs.getInt(LAST_BOTTOM_TAB, 0)
            .takeIf { it > 0 }
            .apply {
                when (this) {
                    1 -> navController.navigate(R.id.navigation_playlists)
                    2 -> navController.navigate(R.id.navigation_playlist)
                }
            }
        presenter.initialise()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (volumeControl.handleVolumeKey(event)) true else super.dispatchKeyEvent(event)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_actionbar, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_paste_add -> startActivity(ShareActivity.intent(this, true))
            R.id.menu_settings -> navigate(R.id.navigation_settings_root)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun checkPlayServices() {
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
        checkForPendingNavigation(null)?.apply { navMapper.navigate(this) }
    }

    override fun onStart() {
        super.onStart()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        presenter.onStart()
        //checkIntent(intent)
        checkForPendingNavigation(null)?.apply { navMapper.navigate(this) }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun isRecreating() = isChangingConfigurations

    override fun showMessage(msg: String) {
        snackBarWrapper.make(msg)
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        when (pref.title) {
            getString(R.string.prefs_root_backup_item_title) -> navController.navigate(R.id.navigation_settings_backup)
        }
        return true
    }

    override fun navigate(destination: NavigationModel) {
        navMapper.navigate(destination)
    }

    override fun navigate(id: Int) {
        navController.navigate(id)
    }

    override fun checkForPendingNavigation(target: Target?): NavigationModel? {
        log.d("checkForPendingNavigation:$target > ${intent.getStringExtra(Target.KEY)}")
        return intent.getStringExtra(Target.KEY)
            ?.takeIf { target == null || it == target.name }
            ?.let {
                return when (it) {
                    PLAYLIST.name ->
                        PlaylistContract.makeNav(
                            PLAYLIST_ID.getLong(intent)
                                ?: throw IllegalArgumentException("Playlist ID is required"),
                            PLAYLIST_ITEM_ID.getLong(intent),
                            PLAY_NOW.getBoolean(intent),
                            SOURCE.getEnum<Source>(intent)
                        ).apply {
                            log.d("got nav:$this")
                        }
                    else -> null
                }
            }
    }

    override fun clearPendingNavigation(target: Target) {
        navMapper.clearArgs(intent, target)
    }

    override fun navigateDone() {
        navController.popBackStack()
    }

    override fun showPlayer() {
        supportFragmentManager
            .beginTransaction()
            //.show(binding.castPlayerFragment.findFragment())
            .show(playerFragment)
            .commitAllowingStateLoss()
        binding.navHostFragment.setPadding(
            0,
            0,
            0,
            res.getDimensionPixelSize(R.dimen.main_navhost_bottom_padding_player)
        )
    }

    override fun hidePlayer() {
        supportFragmentManager
            .beginTransaction()
            //.hide(binding.castPlayerFragment.findFragment())
            .hide(playerFragment)
            .commitAllowingStateLoss()
        binding.navHostFragment.setPadding(0, 0, 0, 0)
    }

    companion object {
        val TOP_LEVEL_DESTINATIONS =
            setOf(R.id.navigation_browse, R.id.navigation_playlists, R.id.navigation_playlist)
        private const val SERVICES_REQUEST_CODE = 1
    }
}
