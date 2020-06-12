package uk.co.sentinelweb.cuer.app.ui.share

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.util.cast.listener.ChromecastYouTubePlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
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
            } ?: linkError(uriString)
    }

    override fun linkError(clipText: String?) {
        log.e("cannot add url : $clipText")
        clipText?.apply {
            view.warning("Cannot add : ${this.take(100)}")
            view.setData(mapEmptyState())
        } ?: run {
            view.warning("Nothing to add ...")
            view.setData(mapEmptyState())
        }
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
        val isConnected = ytContextHolder.isConnected()
        val isNew = state.media?.id?.isBlank() ?: true
        return if (isNew) {
            ShareModel(
                topRightButtonAction = { finish(add = true, play = true, forward = true) },
                topRightButtonText = if (isConnected) res.getString(R.string.share_button_play_now) else res.getString(
                    R.string.share_button_play_locally
                ),
                topRightButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                topLeftButtonAction = { finish(add = true, play = true, forward = false) },
                topLeftButtonText = if (isConnected) res.getString(R.string.share_button_play_return) else null,
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
                topRightButtonText = if (isConnected) res.getString(R.string.share_button_play_now) else res.getString(
                    R.string.share_button_play_locally
                ),
                topRightButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                topLeftButtonAction = { finish(add = false, play = true, forward = false) },
                topLeftButtonText = if (isConnected) res.getString(R.string.share_button_play_return) else null,
                topLeftButtonIcon = if (isConnected) R.drawable.ic_notif_status_cast_conn_white else R.drawable.ic_button_play_black,
                bottomRightButtonAction = { finish(add = false, play = false, forward = true) },
                bottomRightButtonText = res.getString(R.string.share_button_goto_item),
                bottomRightButtonIcon = R.drawable.ic_button_forward_black,
                bottomLeftButtonAction = { finish(add = false, play = false, forward = false) },
                bottomLeftButtonText = res.getString(R.string.share_button_return),
                bottomLeftButtonIcon = R.drawable.ic_button_back_black,
                media = state.media,
                isNewVideo = isNew
            )
        }
    }

    private fun mapEmptyState() =
        ShareModel(
            topRightButtonAction = {},
            topRightButtonText = null,
            topRightButtonIcon = 0,
            topLeftButtonAction = { },
            topLeftButtonText = null,
            topLeftButtonIcon = 0,
            bottomRightButtonAction = { finish(add = false, play = false, forward = true) },
            bottomRightButtonText = res.getString(R.string.share_button_goto_app),
            bottomRightButtonIcon = R.drawable.ic_button_forward_black,
            bottomLeftButtonAction = { finish(add = false, play = false, forward = false) },
            bottomLeftButtonText = res.getString(R.string.share_button_return),
            bottomLeftButtonIcon = R.drawable.ic_button_back_black,
            media = null,
            isNewVideo = false
        )

    private fun finish(add: Boolean, play: Boolean, forward: Boolean) {
        state.jobs.add(CoroutineScope(contextProvider.Main).launch {
            if (add) {
                state.media
                    ?.also { repository.save(it) }
                    ?.also { queue.refreshQueue() }
            }
            val isConnected = ytContextHolder.isConnected()
            if (forward) {
                view.gotoMain(state.media, play)
                view.exit()
            } else { // return play is hidden for not connected
                play.takeIf { it }
                    ?.takeIf { isConnected }
                    ?.let { state.media }
                    ?.also {
                        queue.getItemFor(it.url)
                            ?.run { queue.onItemSelected(this) }
                            .run { view.exit() }

                    } ?: view.exit()
            }
        })
    }

    override fun onStop() {
        state.jobs.forEach { it.cancel() }
    }

    private fun errorLoading(token: String) {
        "Couldn't get youtube info for $token".apply {
            log.e(this)
            view.warning(this)
        }
    }

}