package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart.*

class SharePresenter constructor(
    private val view: ShareContract.View,
    private val repository: MediaDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val contextProvider: CoroutineContextProvider,
    private val ytInteractor: YoutubeInteractor,
    private val toast: ToastWrapper,
    private val queue: QueueMediatorContract.Mediator,
    private val state: ShareState,
    private val ytContextHolder: ChromecastYouTubePlayerContextHolder,
    private val log: LogWrapper,
    private val res: ResourceWrapper

) : ShareContract.Presenter {

    init {
        log.tag = "SharePresenter"
    }

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                state.jobs.add(CoroutineScope(contextProvider.Main).launch {
                    loadOrInfo(scannedMedia)
                        ?.also {
                            loadMedia(it)
                            it.id?.apply { view.warning("Video already in queue ...") }
                        }
                        ?: errorLoading(scannedMedia.url)
                })
            } ?: unableExit(uriString)
    }

    private suspend fun loadOrInfo(scannedMedia: MediaDomain): MediaDomain? = scannedMedia.let {
        repository.loadList(MediaDatabaseRepository.MediaIdFilter(scannedMedia.mediaId))
    }.takeIf { it.isSuccessful }
        ?.let { it.data?.firstOrNull() }
        ?: run {
            ytInteractor.videos(
                ids = listOf(scannedMedia.mediaId),
                parts = listOf(ID, SNIPPET, CONTENT_DETAILS)
            ).takeIf { it.isSuccessful }
                ?.let {
                    it.data?.firstOrNull()
                }
        }

    private fun loadMedia(it: MediaDomain) {
        state.media = it
        view.setData(mapShareModel())
    }

    private fun mapShareModel(): ShareModel {
        val isConnected = ytContextHolder.get()?.isConnected() ?: false
        val isNew = state.media?.id?.isBlank() ?: true
        return if (isNew) {
            ShareModel(
                topRightButtonAction = { finish(add = true, play = true, forward = true) },
                topRightButtonText = res.getString(R.string.share_button_play_now),
                topRightButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                topLeftButtonAction = { finish(add = true, play = true, forward = false) },
                topLeftButtonText = res.getString(R.string.share_button_play_now),
                topLeftButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                bottomRightButtonAction = { finish(add = true, play = false, forward = true) },
                bottomRightButtonText = res.getString(R.string.share_button_add_to_queue),
                bottomRightButtonIcon = R.drawable.ic_button_add_black,
                bottomLeftButtonAction = { finish(add = true, play = false, forward = false) },
                bottomLeftButtonText = res.getString(R.string.share_button_add_return),
                bottomLeftButtonIcon = R.drawable.ic_button_add_black,
                media = state.media,
                isNewVideo = isNew
            )
        } else {
            ShareModel(
                topRightButtonAction = { finish(add = false, play = true, forward = true) },
                topRightButtonText = res.getString(R.string.share_button_play_now),
                topRightButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                topLeftButtonAction = { finish(add = false, play = true, forward = false) },
                topLeftButtonText = res.getString(R.string.share_button_play_now),
                topLeftButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                bottomRightButtonAction = { finish(add = false, play = false, forward = true) },
                bottomRightButtonText = "Go to app",
                bottomRightButtonIcon = R.drawable.ic_button_forward_black,
                bottomLeftButtonAction = { finish(add = false, play = false, forward = false) },
                bottomLeftButtonText = "Return",
                bottomLeftButtonIcon = R.drawable.ic_button_back_black,
                media = state.media,
                isNewVideo = isNew
            )
        }
    }

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            if (add) {
                state.media
                    ?.also { repository.save(it) }
            }
            if (forward) {
                view.gotoMain(state.media, play)
                view.exit()
            } else {
                val isConnected = ytContextHolder.get()?.isConnected() ?: false
                if (isConnected) {
                    queue.refreshQueue()
                    //queue.onItemSelected()// todo
                    if (play) {
                        toast.show("TODO check playing")
                    }
                }
                view.exit()
            }
        })
    }


    private fun errorLoading(token: String) {
        view.exit()
        toast.show("Couldn't load item: $token")
        log.d("Couldn't load item: $token")
    }

    private fun unableExit(uri: String) {
        view.error("Unable to process link $uri")
        view.exit()
    }

}