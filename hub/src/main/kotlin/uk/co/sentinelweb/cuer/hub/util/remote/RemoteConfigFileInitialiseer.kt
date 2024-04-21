package uk.co.sentinelweb.cuer.hub.util.remote

import uk.co.sentinelweb.cuer.app.db.repository.file.ConfigDirectory
import java.io.File

class RemoteConfigFileInitialiseer {

    fun initIfNecessary() {
        println("Application Config Directory: ${ConfigDirectory.Path}")
        File(ConfigDirectory.Path)
            .takeIf { !it.exists() }
            ?.also {
                it.mkdirs()
                println("Created: ${it.absolutePath}")
            }
    }
}