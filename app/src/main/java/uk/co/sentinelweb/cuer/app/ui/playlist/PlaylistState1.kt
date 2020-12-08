package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import uk.co.sentinelweb.cuer.domain.MediaDomain

data class PlaylistState1 constructor(
    var addedMedia: MediaDomain? = null,
    var playAddedAfterRefresh: Boolean = false,
    var focusIndex: Int? = null,
    var lastFocusIndex: Int? = null,
    val jobs: MutableList<Job> = mutableListOf(),
    var deletedMedia: MediaDomain? = null
) : ViewModel()