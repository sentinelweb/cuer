package uk.co.sentinelweb.cuer.app.ui.share

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
import uk.co.sentinelweb.cuer.app.util.extension.serialise
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class ShareActivity : AppCompatActivity(), ShareContract.View {

    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()
    private val snackbarWrapper: SnackbarWrapper by currentScope.inject()

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

    override fun exit() {
        finish()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onResume() {
        super.onResume()
        shareWrapper.getLinkFromIntent(intent) {
            presenter.fromShareUrl(it)
        }
    }

    override fun gotoMain(media: MediaDomain?, play: Boolean) {
        startActivity(// todo map in NavigationMapper
            Intent(this, MainActivity::class.java).apply {
                media?.let {
                    putExtra(MEDIA.toString(), it.serialise())
                    if (play) putExtra(PLAY_NOW.toString(), true)
                }
            })
    }

    override fun setData(model: ShareModel) {
        model.media?.apply { editFragment.setData(this) }

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
                        res = get()
                    )
                }
                scoped { ShareWrapper(getSource()) }
                scoped { SnackbarWrapper(getSource()) }
                viewModel { ShareState() }
            }
        }
    }

}
