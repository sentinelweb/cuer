package uk.co.sentinelweb.cuer.app.ui.share.scan

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.co.sentinelweb.cuer.app.orchestrator.MediaOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.*
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.*
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistItemOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.PlaylistOrchestrator
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.SHARED_PLAYLIST
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
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
    private val linkScanner: LinkScanner,
    private val log: LogWrapper
) : ScanContract.Presenter {

    init {
        log.tag(this)
    }

    override fun fromShareUrl(uriString: String) {
        linkScanner
            .scan(uriString)
            ?.let { scannedMedia ->
                state.viewModelScope.launch {
                    when (scannedMedia.first) {
                        MEDIA -> (scannedMedia.second as MediaDomain).apply {
                            view.setModel(modelMapper.map(this))
                            view.setResult(checkMedia(uriString, this))
                        }
                        PLAYLIST -> (scannedMedia.second as PlaylistDomain).apply {
                            view.setModel(modelMapper.map(scannedMedia.second as PlaylistDomain))
                            log.d("Scanned Playlist = $this")
                            checkPlaylist(uriString, this)
                                ?.apply { view.setResult(this) }
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

    private suspend fun checkMedia(uriString: String, scannedMedia: MediaDomain): ScanContract.Result =// todo return playlistItem if exists
        scannedMedia.let {
            mediaOrchestrator.loadList(PlatformIdListFilter(listOf(scannedMedia.platformId)), Options(LOCAL))
        }.firstOrNull()
            ?.let { media ->
                playlistItemOrchestrator
                    .loadList(MediaIdListFilter(listOf(media.id!!)), Options(LOCAL))
                    .let {
                        modelMapper.mapMediaResult(uriString, false, it.size > 0, media)
                    }
            }
            ?: let {
                modelMapper.mapMediaResult(uriString, true, false, scannedMedia)
            }

    private suspend fun checkPlaylist(uriString: String, scannedPlaylist: PlaylistDomain): ScanContract.Result? {
        try {// todo make orchestrator
            return (scannedPlaylist.platformId
                ?.let {
                    playlistOrchestrator.load(it, Options(LOCAL))
                        ?.also { log.d("found playlist = $it") }
                        ?.let { it to false }
                        ?: playlistOrchestrator.load(it, Options(PLATFORM))
                            ?.copy(id = SHARED_PLAYLIST, config = scannedPlaylist.config.copy(playable = false))
                            ?.also { log.d("loaded playlist = ${it.title} id = ${it.id} platformId = ${it.id}") }
                            ?.let { playlistOrchestrator.save(it, Options(MEMORY, flat = false, emit = false)) to true }
                        ?: throw DoesNotExistException()
                })?.let { (playlist, isNew) ->
                    modelMapper.mapPlaylistResult(uriString, isNew, playlist)
                }
        } catch (e: Exception) {
            log.e("Caught Error loading playlist", e)
            view.showError("${e.message}")
            return null
        }
    }
}