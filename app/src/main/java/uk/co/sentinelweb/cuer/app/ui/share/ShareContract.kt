package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.domain.MediaDomain


interface ShareContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
    }

    interface View {
        fun exit()
        fun gotoMain(media: MediaDomain?, play: Boolean = false)
        fun setData(model: ShareModel)
        fun error(msg: String)
        fun warning(msg: String)
    }
}