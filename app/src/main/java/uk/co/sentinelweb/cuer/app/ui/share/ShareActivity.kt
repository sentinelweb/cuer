package uk.co.sentinelweb.cuer.app.ui.share

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_share.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanFragmentDirections
import uk.co.sentinelweb.cuer.app.util.cast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ShareActivity : AppCompatActivity(), ShareContract.View, ScanContract.Listener {
    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val volumeControl: CuerSimpleVolumeController by inject()

    private lateinit var navController: NavController
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private var snackbar: Snackbar? = null

    private val scanFragment: ScanContract.View? by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as ScanContract.View?)!! }
    }

    private val commitFragment: ShareContract.Committer<*>? by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as ShareContract.Committer<*>?)!! }
            ?: throw IllegalStateException("Not a commit fragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        scanFragment!!.listener = this
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean =
        if (volumeControl.handleVolumeKey(event)) true else super.dispatchKeyEvent(event)

    override fun error(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make("ERROR: $msg").apply { show() }
    }

    override fun warning(msg: String) {
        msg.apply {
            share_warning.setText(msg)
            share_warning.isVisible = true
        }
    }

    override fun exit() {
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus && intent.getBooleanExtra(EXTRA_PASTE, false)) {
            clipboard.getPrimaryClip()
                ?.getItemAt(0)
                ?.text
                ?.apply { scanFragment?.fromShareUrl(this.toString()) ?: throw IllegalStateException("Scan fragment not visible") }
                ?: presenter.linkError(
                    clipboard.getPrimaryClip()
                        ?.getItemAt(0)?.text?.toString()
                )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!intent.getBooleanExtra(EXTRA_PASTE, false)) {
            shareWrapper.getLinkFromIntent(intent) {
                scanFragment?.fromShareUrl(it) ?: throw IllegalStateException("Scan fragment not visible")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun gotoMain(playlistItemDomain: PlaylistItemDomain?, play: Boolean) {
        startActivity( // todo map in NavigationMapper
            Intent(this, MainActivity::class.java).apply {
                playlistItemDomain?.let {
                    putExtra(Target.KEY, PLAYLIST_FRAGMENT.name)
                    putExtra(PLAYLIST_ITEM.name, it.serialise())
                    if (play) putExtra(PLAY_NOW.name, true)
                }
            })
    }

    override fun setData(model: ShareContract.Model) {
        //model.media.apply { editFragment.setData(this) }

        top_left_button.apply {
            isVisible = model.topLeftButtonText?.isNotBlank() ?: false
            setOnClickListener { model.topLeftButtonAction() }
            setText(model.topLeftButtonText)
            setIconResource(model.topLeftButtonIcon)
        }

        bottom_left_button.apply {
            isVisible = model.bottomLeftButtonText?.isNotBlank() ?: false
            setOnClickListener { model.bottomLeftButtonAction() }
            setText(model.bottomLeftButtonText)
            setIconResource(model.bottomLeftButtonIcon)
        }

        top_right_button.apply {
            isVisible = model.topRightButtonText?.isNotBlank() ?: false
            setOnClickListener { model.topRightButtonAction() }
            top_right_button.setText(model.topRightButtonText)
            top_right_button.setIconResource(model.topRightButtonIcon)
        }

        bottom_right_button.apply {
            isVisible = model.bottomRightButtonText?.isNotBlank() ?: false
            setOnClickListener { model.bottomRightButtonAction() }
            setText(model.bottomRightButtonText)
            setIconResource(model.bottomRightButtonIcon)
        }
    }

    override fun showMedia(itemDomain: PlaylistItemDomain) {
        ScanFragmentDirections.actionGotoPlaylistItem(itemDomain.serialise())
            .apply { navController.navigate(this) }
        //  navOptions { launchSingleTop = true; popUpTo(R.id.navigation_playlist_item_edit, { inclusive = true }) }
    }

    override fun scanResult(result: ScanContract.Result) {
        presenter.scanResult(result)
    }

    override suspend fun commitPlaylistItems() {
        commitFragment?.commit()
    }

    override fun getCommittedItems() =
        commitFragment?.getEditedDomains()
            ?.filterNotNull()


    companion object {

        private const val EXTRA_PASTE = "paste"

        fun intent(c: Context, paste: Boolean = false) =
            Intent(c, ShareActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                if (c is Application) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (paste) {
                    putExtra(EXTRA_PASTE, true)
                }
            }

    }


}
