package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.domain.MediaDomain


interface ShareContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
        fun onAddReturn()
        fun onAddForward()
        fun onPlayNow()
        fun onReject()
    }

    interface View {
        fun exit()
        fun gotoMain(media: MediaDomain?, play: Boolean = false)
        fun setData(media: MediaDomain)
        fun error(msg: String)
    }
}