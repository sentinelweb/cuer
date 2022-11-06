package uk.co.sentinelweb.cuer.app.util.share

import android.content.Intent
import android.content.Intent.*
import androidx.appcompat.app.AppCompatActivity
import uk.co.sentinelweb.cuer.app.usecase.EmailUseCase

class EmailWrapper(
    private val activity: AppCompatActivity
) {

    fun launchEmail(data: EmailUseCase.Data) {
        val intent = Intent(ACTION_SEND)
        intent.setType("text/plain")
        intent.putExtra(EXTRA_EMAIL, data.address)
        intent.putExtra(EXTRA_SUBJECT, data.subject)
        intent.putExtra(EXTRA_TEXT, data.msg)
        activity.startActivity(createChooser(intent, data.title))
    }
}