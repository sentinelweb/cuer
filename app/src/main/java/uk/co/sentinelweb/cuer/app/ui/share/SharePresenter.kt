package uk.co.sentinelweb.cuer.app.ui.share

import uk.co.sentinelweb.cuer.app.repository.MemeRepository
import java.net.URI

class SharePresenter constructor(
    private val view: ShareContract.View
//    , private val repo: MemeRepository
) : ShareContract.Presenter {

    override fun fromShareUrl(uriString: String) {
        val uri = URI(uriString)
        if (uri.host.toLowerCase().endsWith("youtu.be")) {
            view.launchYoutubeVideo(uri.path)
        }
        else if (uri.query != null) {
            parseQuery(uri.query)
                .firstOrNull { it[0] == "v" }
                ?.let {
                    view.launchYoutubeVideo(it[1])
                    view.exit()
                }
                ?: unableExit(uriString)
        } else {
            unableExit(uriString)
        }
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }


    private fun parseQuery(query: String): List<List<String>> {
        return query
            .split("&")
            .map { param -> param.split("=") }
    }

    companion object {

    }
//    override fun fromSourceImgLocal(uri: String) {
//        val createMeme = createSaveImageMeme(uri)
//        view.launchImage(createMeme.id)
//        view.exit()
//    }
//
//    override fun fromSourceDeeplink(uri: String) {
//        val url = URI(uri)
//        when (url.path) {
//            "/image" -> processImageDeeplink(url.query)
//            //"/web" -> processWebDeeplink(url.query)
//            else -> unableExit()
//        }
//    }

}