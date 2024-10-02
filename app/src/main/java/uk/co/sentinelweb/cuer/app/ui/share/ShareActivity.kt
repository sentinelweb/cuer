package uk.co.sentinelweb.cuer.app.ui.share

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.ActivityShareBinding
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.ui.common.inteface.CommitHost
import uk.co.sentinelweb.cuer.app.ui.common.navigation.DoneNavigation
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistMviFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_edit.PlaylistEditFragment
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanFragmentDirections
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.CategoryDomain
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.deserialiseCategory
import uk.co.sentinelweb.cuer.domain.ext.deserialiseGuidIdentifier
import uk.co.sentinelweb.cuer.domain.ext.serialise
import java.io.File

class ShareActivity : AppCompatActivity(),
    ShareContract.View,
    ScanContract.Listener,
    DoneNavigation,
    CommitHost,
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource<ShareActivity>()
    private val presenter: ShareContract.Presenter by inject()
    private val shareWrapper: AndroidShareWrapper by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()

    // todo reenable would have to inject player controls from notif (if available else empty)
    //private val volumeControl: CuerSimpleVolumeController by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()
    private val navRouter: NavigationRouter by inject()
    private val shareNavigationHack: ShareNavigationHack by inject()
    private val log: LogWrapper by inject()

    private lateinit var navController: NavController
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private var snackbar: Snackbar? = null

    private var _binding: ActivityShareBinding? = null
    private val binding: ActivityShareBinding
        get() = _binding ?: throw Exception("Share view not bound")

    private val scanFragment: ScanContract.View
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as? ScanContract.View) }
            ?: throw IllegalStateException("Not a scan fragment")

    private val commitFragment: ShareCommitter
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as? ShareCommitter) }
            ?: throw IllegalStateException("Not a commit fragment")

    private val isOnPlaylist: Boolean
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) is PlaylistMviFragment) }
            ?: false

    private val isOnPlaylistItem: Boolean
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) is PlaylistItemEditFragment) }
            ?: false

    private val isOnPlaylistEdit: Boolean
        get() = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) is PlaylistEditFragment) }
            ?: false

    init {
        log.tag(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState
            ?.getString(STATE_KEY)
            ?.apply { presenter.restoreState(this) }
        _binding = ActivityShareBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
        edgeToEdgeWrapper.doOnApplyWindowInsets(binding.shareRoot) { view, insets, padding ->
            view.updatePadding(
                bottom = padding.bottom + insets.systemWindowInsetBottom
            )
        }
        navController.addOnDestinationChangedListener { _: NavController, _: NavDestination, _: Bundle? ->
            presenter.onDestinationChange()
        }
        //volumeControl.controlView = binding.castPlayerVolume
        scanFragment.listener = this
    }

    override fun onDestroy() {
        //volumeControl.controlView = null
        _binding = null
        snackbar = null
        super.onDestroy()
    }

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
//        if (volumeControl.handleVolumeKey(event)) true else super.dispatchKeyEvent(event)

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && intent.getBooleanExtra(PASTE.toString(), false)) {
            checkIntentParams()
            clipboard.getPrimaryClip()
                ?.getItemAt(0)
                ?.text
                ?.apply {
                    if (!presenter.isAlreadyScanned(this.toString())) {
                        scanFragment.fromShareUrl(this.toString())
                    }
                }
                ?: presenter.linkError(
                    clipboard.getPrimaryClip()
                        ?.getItemAt(0)?.text?.toString()
                )
        }
    }

    // fixme: this was added as the intent was delivered via onNewIntent if the user use the home ation
// but the nave was in the previous state - so crash trying to get scan fragment
// probably onNewIntent isnt needed now
// but if finish causes problems here then play with getting the nav in the right state
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (shareNavigationHack.isNavigatingInApp.not()) {
            finish()
        }
        // this could be used to reset the nav controller when home is pressed.
        // so in onNewIntent can get scanFragment
        // but need to stop it for things like link launch - whichmans setting up the descriptionView to parse links
        //navController.popBackStack()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent) {
        if (!intent.getBooleanExtra(PASTE.toString(), false)) {
            checkIntentParams()
            (shareWrapper.getLinkFromIntent(intent)
                ?: shareWrapper.getTextFromIntent(intent))?.apply {
                if (!presenter.isAlreadyScanned(this)) {
                    scanFragment.fromShareUrl(this)
                }
            } ?: presenter.linkError(getString(R.string.share_error_no_link))
        }
    }

    private fun checkIntentParams() {
        intent.getStringExtra(PLAYLIST_PARENT.toString())
            ?.let { deserialiseGuidIdentifier(it) }
            ?.also {
                val cat = intent.getStringExtra(CATEGORY.toString())?.let { deserialiseCategory(it) }
                presenter.setPlaylistParent(cat, it)
            }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun gotoMain(plId: Identifier<GUID>, plItemId: Identifier<GUID>?, play: Boolean) {
        MainActivity.start(this, plId, plItemId, play)
    }

    override fun setData(model: ShareContract.Model) {
        binding.topLeftButton.applyButton(model.topLeft)
        binding.bottomLeftButton.applyButton(model.bottomLeft)
        binding.topRightButton.applyButton(model.topRight)
        binding.bottomRightButton.applyButton(model.bottomRight)
    }

    private fun MaterialButton.applyButton(model: ShareContract.Model.Button) {
        isVisible = model.isVisible
        setOnClickListener { model.action() }
        setText(model.text)
        setIconResource(model.icon)
        setEnabled(model.enabled)
    }

    // todo serialize identifiers
    override fun showMedia(itemDomain: PlaylistItemDomain, playlistParentId: Identifier<GUID>?) {
        ScanFragmentDirections.actionGotoPlaylistItem(
            itemDomain.serialise(),
            itemDomain.id?.source.toString(),
            playlistParentId?.id?.value,
            false
        ).apply { navController.navigate(this) }
        //  navOptions { launchSingleTop = true; popUpTo(R.id.navigation_playlist_item_edit, { inclusive = true }) }
    }

    override fun showPlaylist(id: Identifier<GUID>, playlistParentId: Identifier<GUID>?) {
        ScanFragmentDirections.actionGotoPlaylist(
            id.id.value,
            id.source.toString(),
            playlistParentId?.id?.value,
            false
        ).apply { navController.navigate(this) }
    }

    override fun scanResult(result: ScanContract.Result) {
        presenter.scanResult(result)
    }

    override suspend fun commit(afterCommit: ShareCommitter.AfterCommit) =
        commitFragment.commit(afterCommit)

    override fun canCommit(type: ObjectTypeDomain?): Boolean =
        when (type) {
            ObjectTypeDomain.MEDIA -> isOnPlaylistItem
            ObjectTypeDomain.PLAYLIST -> isOnPlaylist
            ObjectTypeDomain.PLAYLIST_ITEM -> isOnPlaylistItem
            ObjectTypeDomain.CHANNEL -> false
            ObjectTypeDomain.IMAGE -> false
            ObjectTypeDomain.SEARCH_LOCAL -> false
            ObjectTypeDomain.SEARCH_REMOTE -> false
            ObjectTypeDomain.PLAYLIST_TREE -> false
            ObjectTypeDomain.UNKNOWN -> false
            null -> false
        }

    override fun error(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make("ERROR: $msg").apply { show() }
    }

    override fun warning(msg: String) = with(binding.shareWarning) {
        text = msg
        isVisible = true
    }

    override fun hideWarning() = with(binding.shareWarning) {
        isVisible = false
    }

    override fun exit() {
        finish()
    }

    // PlaylistItemEditContract.DoneNavigation
    override fun navigateDone() {
        if (commitFragment is PlaylistItemEditFragment) {
            presenter.afterItemEditNavigation()
        } else {
            navigate(NavigationModel(Target.NAV_FINISH))
        }
    }

    override fun navigate(nav: NavigationModel) {
        navRouter.navigate(nav)
    }

    // CommitHost
    override fun isReady(ready: Boolean) {
        log.d("commit host isReady: $ready")
        presenter.onReady(ready)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val serializeState = presenter.serializeState()
        getFilesDir()
            ?.let { File(it, "shareactivity.json") }
            ?.apply { writeText(serializeState ?: "") }
        outState.putString(STATE_KEY, serializeState)
    }

    companion object {
        private const val STATE_KEY = "share_state"

        // todo add parent id if in playlist fragment (called form main atm)
        fun intent(
            c: Context,
            paste: Boolean = false,
            parentId: Long? = null
        ) = Intent(c, ShareActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            if (c is Application) addFlags(FLAG_ACTIVITY_NEW_TASK)
            if (paste) {
                putExtra(PASTE.toString(), true)
            }
            if (parentId != null) {
                this.putExtra(PLAYLIST_PARENT.toString(), parentId)
            }
        }

        fun urlIntent(
            c: Context,
            url: String,
            parentId: Identifier<GUID>? = null,
            fromCategory: CategoryDomain? = null
        ) = Intent(c, ShareActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            if (c is Application) addFlags(FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse(url)
            if (fromCategory != null) {
                this.putExtra(CATEGORY.toString(), fromCategory.serialise())
            }
            if (parentId != null) {
                this.putExtra(PLAYLIST_PARENT.toString(), parentId.serialise())
            }
        }
    }
}
