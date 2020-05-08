package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.domain.MediaDomain


interface ShareContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
    }

    interface View {
        fun exit()
        fun gotoMain(media: MediaDomain?)
        fun error(msg: String)
    }
}