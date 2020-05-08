package uk.co.sentinelweb.cuer.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.serialization.UnstableDefault
import org.koin.android.scope.currentScope
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.Const
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.ui.playlist_item_edit.PlaylistItemEditFragment
import uk.co.sentinelweb.cuer.app.util.extension.serialise
import uk.co.sentinelweb.cuer.domain.MediaDomain

class ShareActivity : AppCompatActivity(), ShareContract.View {

    private val presenter: ShareContract.Presenter by currentScope.inject()
    private val editFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.playlist_edit_fragment) as PlaylistItemEditFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {// todo move in
                    presenter.fromShareUrl(intent.data.toString())
                } else if (intent.clipData?.itemCount ?: 0 > 0) { // todo move in
                    presenter.fromShareUrl(intent.clipData?.getItemAt(0)?.text.toString())
                } else {
                    intent.data?.let { presenter.fromShareUrl(it.toString()) }
                }
            }
            Intent.ACTION_VIEW -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {
                    presenter.fromShareUrl(intent.data.toString())
                } else {
                    intent.data?.let { presenter.fromShareUrl(it.toString()) }
                }
            }
        }

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

    @UnstableDefault
    override fun gotoMain(media: MediaDomain?, play: Boolean) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                media?.let {
                    putExtra(Const.EXTRA_MEDIA, it.serialise())
                    if (play) putExtra(Const.EXTRA_PLAY_NOW, true)
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
                        state = get()
                    )
                }
                viewModel { ShareState() }
            }
        }
    }

}
