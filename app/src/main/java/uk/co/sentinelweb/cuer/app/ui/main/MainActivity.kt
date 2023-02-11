package uk.co.sentinelweb.cuer.app.ui.main

import android.animation.ObjectAnimator
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter
import uk.co.sentinelweb.cuer.app.backup.AutoBackupFileExporter.BackupResult.*
import uk.co.sentinelweb.cuer.app.databinding.ActivityMainBinding
import uk.co.sentinelweb.cuer.app.ext.serialise
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationProvider
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.main.MainCommonContract.LastTab.*
import uk.co.sentinelweb.cuer.app.ui.onboarding.AppInstallHelpConfig
import uk.co.sentinelweb.cuer.app.ui.play_control.CompactPlayerScroll
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.share.ShareActivity
import uk.co.sentinelweb.cuer.app.util.cast.ChromeCastWrapper
import uk.co.sentinelweb.cuer.app.util.cast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferences.ONBOARDED_PREFIX
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.*
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.GUID


class MainActivity :
    AppCompatActivity(),
    MainContract.View,
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
    CompactPlayerScroll.PlayerHost,
    AndroidScopeComponent,
    MainContract.PlayerViewControl {

    override val scope: Scope by activityScopeWithSource<MainActivity>()

    private val presenter: MainContract.Presenter by inject()
    private val chromeCastWrapper: ChromeCastWrapper by inject()
    private val snackBarWrapper: SnackbarWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val log: LogWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val volumeControl: CuerSimpleVolumeController by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val res: ResourceWrapper by inject()
    private val prefs: MultiPlatformPreferencesWrapper by inject()
    private val navigationProvider: NavigationProvider by inject()
    private val multiPlatformPreferences: MultiPlatformPreferencesWrapper by inject()

    private lateinit var navController: NavController

    private var _binding: ActivityMainBinding? = null

    private val isOnboarding: Boolean
        get() = multiPlatformPreferences.getBoolean(ONBOARDED_PREFIX, this::class.simpleName!!, false).not()

    private val binding: ActivityMainBinding
        get() = _binding ?: throw IllegalStateException("ActivityMainBinding not bound")

    private val playerFragment: Fragment
        get() = supportFragmentManager.findFragmentById(R.id.cast_player_fragment)
            ?: throw Exception("No player fragment")

    private var _menu: Menu? = null
    private val pasteAddMenuItem: MenuItem?
        get() = _menu?.findItem(R.id.menu_paste_add)
    private val settingsMenuItem: MenuItem?
        get() = _menu?.findItem(R.id.menu_settings)

    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    init {
        log.tag(this)
    }

    override val playerControls: PlayerContract.PlayerControls by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!isChangingConfigurations) {
            installSplashScreen()
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)

        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .also { navController = it.navController }

        setupBottomNav()

        edgeToEdgeWrapper.doOnApplyWindowInsets(binding.bottomNavView) { view, insets, padding ->
            view.updatePadding(
                bottom = padding.bottom + insets.systemWindowInsetBottom
            )
        }
        navController.addOnDestinationChangedListener { _: NavController, navDestination: NavDestination, bundle: Bundle? ->
            log.d("navigation change: dest: $navDestination bundle:$bundle")
        }

        volumeControl.controlView = binding.castPlayerVolume

        if (isOnboarding) {
            navController.navigate(
                R.id.navigation_onboarding, bundleOf(
                    ONBOARD_CONFIG.name to AppInstallHelpConfig(res).build().serialise(),
                    ONBOARD_KEY.name to MainActivity::class.simpleName!!
                )
            )
        } else {
            restoreBottomNavTab(savedInstanceState != null)
        }
        presenter.initialise()
    }

    override fun promptToBackup(result: AutoBackupFileExporter.BackupResult) {
        if (!isOnboarding) {
            when (result) {
                SUCCESS -> toastWrapper.show(getString(R.string.backup_success_message))
                SETUP -> snackBarWrapper.make(
                    msg = getString(R.string.backup_setup_message),
                    actionText = getString(R.string.backup_setup_action)
                ) {
                    navController.navigate(
                        R.id.navigation_settings_backup, bundleOf(DO_AUTO_BACKUP.name to true)
                    )
                }.positionAbovePlayer().show()

                FAIL -> snackBarWrapper.makeError(
                    msg = getString(R.string.backup_fix_message),
                    actionText = getString(R.string.backup_fix_action)
                ) {
                    navController.navigate(R.id.navigation_settings_backup)
                }.show()
            }
        }
    }

    override fun onDestroy() {
        presenter.onDestroy()
        volumeControl.controlView = null
        _binding = null
        _menu = null
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (volumeControl.handleVolumeKey(event)) true
        else super.dispatchKeyEvent(event)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_actionbar, menu)
        _menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_paste_add -> checkIntentAndPasteAdd()
            R.id.menu_settings -> navController.navigate(R.id.navigation_settings_root)
        }
        return super.onOptionsItemSelected(item)
    }

    fun checkIntentAndPasteAdd() {
        // check clipboard data
        clipboard.getPrimaryClip()
            ?.getItemAt(0)
            ?.text
            ?.takeIf { it.startsWith("http") }
            ?.also { startActivity(ShareActivity.intent(this, true)) }
            ?: also { snackBarWrapper.makeError("There's no url on the clipboard").show() }
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
        navigationProvider.checkForPendingNavigation(null)
            ?.apply { navRouter.navigate(this) }
    }

    override fun onStart() {
        super.onStart()
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        presenter.onStart()
        //checkIntent(intent)
        navigationProvider.checkForPendingNavigation(null)
            ?.apply { navRouter.navigate(this) }

        hidePlayerIfOnboarding()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun isRecreating() = isChangingConfigurations

    override fun showMessage(msg: String) {
        snackBarWrapper.make(msg).show()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference,
    ): Boolean {
        when (pref.key) {
            getString(R.string.prefs_root_backup_key) -> navController.navigate(R.id.action_goto_settings_backup)
            getString(R.string.prefs_root_player_key) -> navController.navigate(R.id.action_goto_settings_player)
        }
        return true
    }

    override fun showPlayer() {
        supportFragmentManager
            .beginTransaction()
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
            .hide(playerFragment)
            .commitAllowingStateLoss()
        binding.navHostFragment.setPadding(0, 0, 0, 0)
    }


    fun isPlayerShowing() = playerFragment.isVisible()


    private fun hidePlayerIfOnboarding() {
        if (isOnboarding) {
            lifecycleScope.launch {
                delay(500)
                hidePlayer()
            }
        }
    }

    var isRaised = true
    override fun lowerPlayer() {
        if (isRaised) {
            val lowerY = res.getDimensionPixelSize(R.dimen.player_lower_y).toFloat()
            val transAnimation =
                ObjectAnimator.ofFloat(binding.castPlayerFragment, "translationY", 0f, lowerY)
            transAnimation.setDuration(200)
            transAnimation.start()
            isRaised = false
        }
    }

    override fun raisePlayer() {
        if (!isRaised) {
            val lowerY = res.getDimensionPixelSize(R.dimen.player_lower_y).toFloat()
            val transAnimation =
                ObjectAnimator.ofFloat(binding.castPlayerFragment, "translationY", lowerY, 0f)
            transAnimation.setDuration(200)
            transAnimation.start()
            isRaised = true
        }
    }

    private fun setupBottomNav() {
//        binding.bottomNavView.setupWithNavController(navController)
        NavigationUI.setupWithNavController(binding.bottomNavView, navController, false)
        binding.bottomNavView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_browse -> BROWSE
                R.id.navigation_playlists -> PLAYLISTS
                R.id.navigation_playlist -> PLAYLIST
                else -> BROWSE
            }.ordinal
                .also { prefs.lastBottomTab = it }
                .also { log.d("set LAST_BOTTOM_TAB: $it") }
            if (navController.currentDestination?.id != it.itemId) {
                navigateToBottomTab(it.itemId)
            }
            true
        }
    }

    private fun restoreBottomNavTab(isConfigChange: Boolean) {
        if (!isConfigChange) {
            prefs.lastBottomTab
                .also { log.d("get LAST_BOTTOM_TAB: $it") }
                //.takeIf { it > 0}
                .also {
                    when (MainCommonContract.LastTab.values()[it]) {
                        PLAYLISTS -> if (navController.currentDestination?.id != R.id.navigation_playlists) {
                            R.id.navigation_playlists
                        } else null

                        PLAYLIST -> if (navController.currentDestination?.id != R.id.navigation_playlist) {
                            R.id.navigation_playlist
                        } else null

                        else -> if (navController.currentDestination?.id != R.id.navigation_browse) {
                            R.id.navigation_browse
                        } else null
                    }?.also { navId ->
                        navigateToBottomTab(navId)
                    }
                }
        }
    }

    private fun navigateToBottomTab(navId: Int) {
        navController.clearBackStack(navId)
        navController.navigate(
            navId,
            args = null,
            navOptions = navOptions {
                launchSingleTop = true
                popUpTo(navId) {
                    inclusive = true
                }
            },
            navigatorExtras = null
        )
    }

    fun finishedOnboarding() {
        navigateToBottomTab(R.id.navigation_browse)
    }

    companion object {
        val TOP_LEVEL_DESTINATIONS =
            setOf(R.id.navigation_browse, R.id.navigation_playlists, R.id.navigation_playlist)
        private const val SERVICES_REQUEST_CODE = 1

        fun start(c: Context, plId: Identifier<GUID>, plItemId: Identifier<GUID>?, play: Boolean) {
            c.startActivity(
                Intent(c, MainActivity::class.java).apply {
                    putExtra(Target.KEY, Target.PLAYLIST.name)
                    putExtra(PLAYLIST_ID.name, plId.id.value)
                    plItemId?.also { putExtra(PLAYLIST_ITEM_ID.name, it.id.value) }
                    putExtra(PLAY_NOW.name, play)
                    putExtra(SOURCE.name, plId.source.toString())
                })
        }

        fun startFromService(c: Context) {
            c.startActivity(Intent(c, MainActivity::class.java)
                .apply { setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
    }
}
