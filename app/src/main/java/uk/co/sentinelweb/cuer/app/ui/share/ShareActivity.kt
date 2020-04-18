package uk.co.sentinelweb.cuer.app.ui.share

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class ShareActivity : AppCompatActivity(), ShareContract.View {

    private lateinit var presenter: ShareContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)
        presenter = SharePresenter(this)
//        presenter = SharePresenter(
//            this,
//            PreferencesMemeRepository(AppFilePath(this), this.applicationContext)
//        )
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {// todo move in
                    presenter.fromShareUrl(intent.data.toString())
                } else if (intent.clipData?.itemCount ?: 0 > 0) { // todo move in
                    presenter.fromShareUrl(intent.clipData?.getItemAt(0)?.text.toString())
                } else {
//                    presenter.fromLocalText(intent.data?.toString())
                }
            }
            Intent.ACTION_VIEW -> {
                if (intent.data?.host?.endsWith("youtube.com") ?: false) {
                    presenter.fromShareUrl(intent.data.toString())
                } else {
//                    presenter.fromLocalText(intent.data?.toString())
                }
            }
        }
    }

    //    override fun launchWeb(memeId: String) {
//        TaskStackBuilder.create(this)
//            .addParentStack(this)
//            .addNextIntent(Intent(this, MemeListActivity::class.java))
//            .addNextIntent(MemeEditActivity.intent(this, memeId))
//            .addNextIntent(WebClipActivity.intent(this, memeId))
//            .startActivities()
//    }
//
//    override fun launchImage(memeId: String) {
//        TaskStackBuilder.create(this)
//            .addParentStack(this)
//            .addNextIntent(Intent(this, MemeListActivity::class.java))
//            .addNextIntent(MemeEditActivity.intent(this, memeId))
//            .addNextIntent(ImageClipActivity.intent(this, memeId, SHARE))
//            .startActivities()
//    }
//
    override fun error(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun exit() {
        finish()
    }

    override fun launchYoutubeVideo(youtubeId: String) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra("YTID", youtubeId)
            })
    }

}
