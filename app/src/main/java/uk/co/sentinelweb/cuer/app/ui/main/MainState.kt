package uk.co.sentinelweb.cuer.app.ui.main

import androidx.lifecycle.ViewModel

data class MainState constructor(
    var playServicesAvailable: Boolean = false,
    var playServiceCheckDone: Boolean = false
) : ViewModel()

