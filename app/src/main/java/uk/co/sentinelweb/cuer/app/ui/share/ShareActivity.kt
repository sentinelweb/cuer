package uk.co.sentinelweb.cuer.app.ui.share

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_share.*
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAYLIST_ITEM
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Target.PLAYLIST_FRAGMENT
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.util.cast.CuerSimpleVolumeController
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ShareActivity : AppCompatActivity(), ShareContract.View {
    // todo add navigation here
    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()
    private val volumeControl: CuerSimpleVolumeController by inject()

    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private var snackbar: Snackbar? = null

    private val editFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.playlist_edit_fragment) as PlaylistItemEditFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
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
                ?.apply { presenter.fromShareUrl(this.toString()) }
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
                presenter.fromShareUrl(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun gotoMain(media: PlaylistItemDomain?, play: Boolean) {
        startActivity( // todo map in NavigationMapper
            Intent(this, MainActivity::class.java).apply {
                media?.let {
                    putExtra(Target.KEY, PLAYLIST_FRAGMENT.name)
                    putExtra(PLAYLIST_ITEM.name, it.serialise())
                    if (play) putExtra(PLAY_NOW.name, true)
                }
            })
    }

    override fun setData(model: ShareModel) {
        model.media.apply { editFragment.setData(this) }

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

    override suspend fun commitPlaylistItems() {
        editFragment.commitPlaylistItems()
    }

    override fun getPlaylistItems(): List<PlaylistItemDomain> = editFragment.getPlaylistItems()

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

        @JvmStatic
        val activityModule = module {
            scope(named<ShareActivity>()) {
                scoped<ShareContract.View> { getSource() }
                scoped<ShareContract.Presenter> {
                    SharePresenter(
                        view = get(),
                        repository = get(),
                        playlistRepository = get(),
                        linkScanner = get(),
                        contextProvider = get(),
                        ytInteractor = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        ytContextHolder = get(),
                        mapper = get(),
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                scoped { ShareWrapper(getSource()) }
                scoped { SnackbarWrapper(getSource()) }
                viewModel { ShareState() }
                scoped {
                    ShareModelMapper(
                        ytContextHolder = get(),
                        res = get()
                    )
                }
            }
        }
    }

}
