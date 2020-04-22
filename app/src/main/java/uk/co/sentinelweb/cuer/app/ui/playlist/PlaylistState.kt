package uk.co.sentinelweb.cuer.app.ui.playlist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job

class PlaylistState constructor(
    val jobs: MutableList<Job> = mutableListOf()
): ViewModel()

