package uk.co.sentinelweb.cuer.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.scope.currentScope
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.Const
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class ShareActivity : AppCompatActivity(), ShareContract.View {

    private val presenter: ShareContract.Presenter by currentScope.inject()

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
    }

    override fun error(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun exit() {
        finish()
    }

    override fun gotoMain(youtubeId: String?) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                youtubeId?.let { putExtra(Const.EXTRA_YTID, it) }
            })
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
                        toast = get()
                    )
                }
            }
        }
    }

}
