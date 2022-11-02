package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class EmailUseCase(
    private val res: ResourceWrapper
) {

    fun makeFeedbackEmail(): Data = Data(
        title = res.getString(R.string.email_title_choose),
        address = res.getString(R.string.app_email),
        subject = res.getString(R.string.email_feedback_subject),
        msg = res.getString(R.string.email_feedback_body),
    )

    data class Data(
        val title: String,
        val address: String,
        val subject: String,
        val msg: String
    )
}