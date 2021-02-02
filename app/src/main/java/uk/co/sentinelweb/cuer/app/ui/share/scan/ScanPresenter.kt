package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.MEDIA
import uk.co.sentinelweb.cuer.domain.ObjectTypeDomain.PLAYLIST
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class ScanPresenter(
    private val view: ScanContract.View,
    private val state: ScanContract.State,
    private val modelMapper: ScanMapper,
    private val mediaOrchestrator: MediaOrchestrator,
    private val playlistOrchestrator: PlaylistOrchestrator,
    private val playlistItemOrchestrator: PlaylistItemOrchestrator,
    private val linkScanner: LinkScanner
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
                            checkPlaylist(this)?.apply { view.setResult(this) }
                                ?: linkError(uriString)
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
            mediaOrchestrator.loadList(PlatformIdListFilter(listOf(scannedMedia.platformId)), Options(LOCAL))
        }.firstOrNull()
            ?.let { media ->
                playlistItemOrchestrator
                    .loadList(MediaIdListFilter(listOf(media.id!!)), Options(LOCAL))
                    .let {
                        modelMapper.mapMediaResult(false, it.size > 0, media)
                    }
            }
            ?: let {
                modelMapper.mapMediaResult(false, false, scannedMedia)
            }

    private suspend fun checkPlaylist(scannedPlaylist: PlaylistDomain): ScanContract.Result? =
        (scannedPlaylist.platformId
            ?.let {
                playlistOrchestrator.loadList(PlatformIdListFilter(listOf(it)), Options(LOCAL))
                    .takeIf { it.size > 0 }
                    ?.get(0)
                    ?.let { it to false }
                    ?: playlistOrchestrator.loadList(PlatformIdListFilter(listOf(it)), Options(PLATFORM))
                        .takeIf { it.size > 0 }
                        ?.get(0)
                        ?.let { playlistOrchestrator.save(it, Options(MEMORY, false)) to true }
                    ?: throw DoesNotExistException()

            })?.let { (playlist, isNew) ->
                modelMapper.mapPlaylistResult(isNew, playlist)
            }
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