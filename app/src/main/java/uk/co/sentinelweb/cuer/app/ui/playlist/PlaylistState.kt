package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

data class PlaylistState constructor(
    var focusItemId: String? = null,
    val jobs: MutableList<Job> = mutableListOf()
) : ViewModel()

