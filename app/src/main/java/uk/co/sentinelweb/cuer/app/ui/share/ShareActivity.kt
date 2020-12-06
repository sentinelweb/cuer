package uk.co.sentinelweb.cuer.app.ui.share

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_share.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.PLAY_NOW
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.util.share.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class ShareActivity : AppCompatActivity(), ShareContract.View {
    // todo add navigation here
    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()

    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    private var snackbar: Snackbar? = null

    private val editFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.playlist_edit_fragment) as PlaylistItemEditFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
    }

    override fun error(msg: String) {
        snackbar?.dismiss()
        snackbar = snackbarWrapper.make("ERROR: $msg")
    }

    override fun warning(msg: String) {
        msg.apply {
            share_warning.setText(msg)
            share_warning.isVisible = true
        }
    }

//    override fun showPlaylistEdit() {
//        // todo remove this and just make a text edit in the item edit
//        supportFragmentManager.beginTransaction().apply {
//            val playlistEditFragment = PlaylistEditFragment()
//            add(playlistEditFragment,"pedit")
//            remove(editFragment)
//            show(playlistEditFragment)
//            commit()
//        }
//
//    }

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

    override fun gotoMain(media: MediaDomain?, play: Boolean) {
        startActivity( // todo map in NavigationMapper
            Intent(this, MainActivity::class.java).apply {
                media?.let {
                    putExtra(MEDIA.toString(), it.serialise())
                    if (play) putExtra(PLAY_NOW.toString(), true)
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
                        linkScanner = get(),
                        contextProvider = get(),
                        ytInteractor = get(),
                        toast = get(),
                        queue = get(),
                        state = get(),
                        log = get(),
                        ytContextHolder = get(),
                        mapper = get()
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
