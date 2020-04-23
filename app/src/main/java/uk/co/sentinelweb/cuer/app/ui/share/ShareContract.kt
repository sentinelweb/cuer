package uk.co.sentinelweb.cuer.app.ui.share


interface ShareContract {

    interface Presenter {
        fun fromShareUrl(uriString: String)
    }

    interface View {
        fun exit()
        fun gotoMain(youtubeId: String?)
        fun error(msg: String)
    }
}