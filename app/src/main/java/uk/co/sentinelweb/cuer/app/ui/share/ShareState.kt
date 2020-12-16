package uk.co.sentinelweb.cuer.app.ui.share

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

@Serializable
data class ShareState constructor(
    var media: MediaDomain? = null,
    var playlistItems: List<PlaylistItemDomain>? = null,
    @Transient var model: ShareModel? = null,
    @Transient val jobs: MutableList<Job> = mutableListOf()
) : ViewModel()

