package uk.co.sentinelweb.cuer.app.ui.share

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import uk.co.sentinelweb.cuer.domain.MediaDomain

@Serializable
data class ShareState constructor(
    var media: MediaDomain? = null,
    @Transient val jobs: MutableList<Job> = mutableListOf()
) : ViewModel()

