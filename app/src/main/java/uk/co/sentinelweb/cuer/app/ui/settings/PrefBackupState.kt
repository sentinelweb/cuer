package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.lifecycle.ViewModel
import java.time.Instant

data class PrefBackupState constructor(
    var lastBackedUp: Instant? = null,
    var writeData: String? = null
) : ViewModel()