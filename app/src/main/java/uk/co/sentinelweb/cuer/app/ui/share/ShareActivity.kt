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
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_share.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.*
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanContract
import uk.co.sentinelweb.cuer.app.ui.share.scan.ScanFragmentDirections
import uk.co.sentinelweb.cuer.app.util.cast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.WindowWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ShareActivity : AppCompatActivity(), ShareContract.View, ScanContract.Listener, PlaylistItemEditContract.DoneNavigation {
    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val volumeControl: CuerSimpleVolumeController by inject()
    private val windowWrapper: WindowWrapper by inject()
    private val navMapper: NavigationMapper by currentScope.inject()

    private lateinit var navController: NavController
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private var snackbar: Snackbar? = null

    private val scanFragment: ScanContract.View? by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as ScanContract.View?)!! }
    }

    private val commitFragment: ShareContract.Committer by lazy {
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            ?.run { (getChildFragmentManager().getFragments().get(0) as ShareContract.Committer?)!! }
            ?: throw IllegalStateException("Not a commit fragment")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //windowWrapper.setDecorFitsSystemWindows(this, true)
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
            (shareWrapper.getLinkFromIntent(intent) ?: shareWrapper.getTextFromIntent(intent))?.apply {
                scanFragment?.fromShareUrl(this) ?: throw IllegalStateException("Scan fragment not visible")
            } ?: presenter.linkError("Could not find a link to process")
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun gotoMain(plId: Long, plItemId: Long?, source: Source, play: Boolean) {//playlistItemDomain: PlaylistItemDomain?,
        startActivity( // todo map in NavigationMapper
            Intent(this, MainActivity::class.java).apply {
                putExtra(Target.KEY, PLAYLIST_FRAGMENT.name)
                putExtra(PLAYLIST_ID.name, plId)
                plItemId?.also { putExtra(PLAYLIST_ITEM_ID.name, it) }
                putExtra(PLAY_NOW.name, play)
                putExtra(SOURCE.name, source.toString())
            })
    }

    override fun setData(model: ShareContract.Model) {
        top_left_button.applyButton(model.topLeft)
        bottom_left_button.applyButton(model.bottomLeft)
        top_right_button.applyButton(model.topRight)
        bottom_right_button.applyButton(model.bottomRight)
    }

    private fun MaterialButton.applyButton(model: ShareContract.Model.Button) {
        isVisible = model.isVisible
        setOnClickListener { model.action() }
        setText(model.text)
        setIconResource(model.icon)
    }

    override fun showMedia(itemDomain: PlaylistItemDomain, source: Source) {
        ScanFragmentDirections.actionGotoPlaylistItem(itemDomain.serialise(), source.toString())
            .apply { navController.navigate(this) }
        //  navOptions { launchSingleTop = true; popUpTo(R.id.navigation_playlist_item_edit, { inclusive = true }) }
    }

    override fun showPlaylist(id: OrchestratorContract.Identifier<Long>) {
        ScanFragmentDirections.actionGotoPlaylist(id.id, id.source.toString())
            .apply { navController.navigate(this) }
    }

    override fun scanResult(result: ScanContract.Result) {
        presenter.scanResult(result)
    }

    override suspend fun commit(onCommit: ShareContract.Committer.OnCommit) =
        commitFragment.commit(onCommit)

//
//    override fun getCommittedItems() =
//        commitFragment?.getEditedDomains()
//            ?.filterNotNull()

    // PlaylistItemEditContract.DoneNavigation
    override fun navigateDone() {
        presenter.afterItemEditNavigation()
    }

    override fun navigate(nav: NavigationModel) {
        navMapper.map(nav)
    }

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
