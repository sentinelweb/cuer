package uk.co.sentinelweb.cuer.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_share.*
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.NavigationModel.NavigateParam.MEDIA
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.util.extension.serialise
import uk.co.sentinelweb.cuer.app.util.wrapper.ShareWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain

class ShareActivity : AppCompatActivity(), ShareContract.View {

    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val shareWrapper: ShareWrapper by currentScope.inject()

    private val editFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.playlist_edit_fragment) as PlaylistItemEditFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)


        add_return_button.setOnClickListener { presenter.onAddReturn() }
        add_forward_button.setOnClickListener { presenter.onAddForward() }
        play_now_button.setOnClickListener { presenter.onPlayNow() }
        reject_button.setOnClickListener { presenter.onReject() }
    }

    override fun error(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun exit() {
        finish()
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
                    if (play) putExtra(MEDIA.toString(), true)
                }
            })
    }

    override fun setData(media: MediaDomain) {
        editFragment.setData(media)
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
                        log = get()
                    )
                }
                scoped { ShareWrapper(getSource()) }
                viewModel { ShareState() }
            }
        }
    }

}
