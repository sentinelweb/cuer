package uk.co.sentinelweb.cuer.domain.update

import kotlinx.datetime.Instant
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.domain.GUID
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain


abstract class UpdateDomain<T>

sealed class MediaUpdateDomain : UpdateDomain<MediaDomain>()

// todo move inside sealed class?
data class MediaPositionUpdateDomain(
    val id: OrchestratorContract.Identifier<GUID>,
    val duration: Long?,
    val positon: Long?,
    val dateLastPlayed: Instant?,
    val watched: Boolean
) : MediaUpdateDomain()

sealed class PlaylistUpdateDomain : UpdateDomain<PlaylistDomain>()

data class PlaylistIndexUpdateDomain(
    val id: OrchestratorContract.Identifier<GUID>,
    val currentIndex: Int,
) : PlaylistUpdateDomain()