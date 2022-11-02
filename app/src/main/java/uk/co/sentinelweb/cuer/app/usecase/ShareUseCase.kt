package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class ShareUseCase(
    private val res: ResourceWrapper
) {

    fun shareApp() = Data(
        title = res.getString(R.string.share_title_choose),
        text = res.getString(R.string.share_body),
        uri = res.getString(R.string.share_uri),
        subject = res.getString(R.string.share_subject),
    )

    data class Data(
        val title: String,
        val text: String,
        val uri: String,
        val subject: String,
    )
}