package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.db.repository.MediaDatabaseRepository
import uk.co.sentinelweb.cuer.app.db.repository.PlaylistDatabaseRepository
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor

class ScanPresenter(
    private val view: ScanContract.View,
    private val state: ScanContract.State,
    private val modelMapper: ScanMapper,
    private val mediaDatabaseRepository: MediaDatabaseRepository,
    private val playlistDatabaseRepository: PlaylistDatabaseRepository,
    private val linkScanner: LinkScanner,
    private val ytInteractor: YoutubeInteractor
) : ScanContract.Presenter {

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                state.viewModelScope.launch {
                    when (scannedMedia.first) {
                        MEDIA -> (scannedMedia.second as MediaDomain).apply {
                            view.setModel(modelMapper.map(this))
                            view.setResult(checkMedia(this))
                        }
                        PLAYLIST -> (scannedMedia.second as PlaylistDomain).apply {
                            view.setModel(modelMapper.map(scannedMedia.second as PlaylistDomain))
                            view.setResult(checkPlaylist(this))
                        }
                        else -> linkError(uriString)

                    }

                }
            } ?: linkError(uriString)
    }

    private fun linkError(uriString: String) {
        view.showMessage("Cannot parse: $uriString")
        view.setModel(modelMapper.mapError(uriString))
    }

    private suspend fun checkMedia(scannedMedia: MediaDomain): ScanContract.Result =// todo return playlistItem if exists
        scannedMedia.let {
            mediaDatabaseRepository.loadList(OrchestratorContract.PlatformIdListFilter(listOf(scannedMedia.platformId)))
        }.takeIf { it.isSuccessful }
            ?.let { it.data?.firstOrNull() }
            ?.let { media ->
                playlistDatabaseRepository
                    .loadPlaylistItems(OrchestratorContract.MediaIdListFilter(listOf(media.id!!)))
                    .takeIf { it.isSuccessful }
                    ?.let {
                        modelMapper.mapMediaResult(false, it.data?.size ?: 0 > 0, media)
                    }
            }
            ?: let {
                modelMapper.mapMediaResult(false, false, scannedMedia)
            }

    private suspend fun checkPlaylist(scannedPlaylist: PlaylistDomain): ScanContract.Result =
    // todo add feild to check for platformId
//        scannedPlaylist.let {
//                playlistDatabaseRepository
//                    .loadList(PlaylistDatabaseRepository.MediaIdListFilter(listOf(media.id!!)))
//                    .takeIf { it.isSuccessful }
//                    ?.let {
//                        modelMapper.mapMediaResult(false, it.data?.size ?: 0 > 0, media)
//                    }
//            }
//            ?: let {
        modelMapper.mapPlaylistResult(true, scannedPlaylist)
//            }


//
//    private suspend fun loadOrInfo(scannedMedia: MediaDomain): MediaDomain? =
//        scannedMedia.let {
//            mediaDatabaseRepository.loadList(MediaDatabaseRepository.MediaIdFilter(scannedMedia.platformId))
//        }.takeIf { it.isSuccessful }
//            ?.let { it.data?.firstOrNull() }
//            ?: run {
//                ytInteractor.videos(
//                    ids = listOf(scannedMedia.platformId),
//                    parts = listOf(YoutubePart.ID, YoutubePart.SNIPPET, YoutubePart.CONTENT_DETAILS)
//                ).takeIf { it.isSuccessful }
//                    ?.let {
//                        it.data?.firstOrNull()
//                    }
//            }
}