package uk.co.sentinelweb.cuer.app.factory

import com.arkivanov.essenty.lifecycle.Lifecycle
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseController
import uk.co.sentinelweb.cuer.app.ui.playlists.PlaylistsMviController

class PresentationFactory : KoinComponent {

    fun browseControllerCreate(lifecycle: Lifecycle): BrowseController {
        return get(parameters = { parametersOf(lifecycle) })
    }

    fun playlistsController(lifecycle: Lifecycle): PlaylistsMviController {
        return get(parameters = { parametersOf(lifecycle) })
    }
}