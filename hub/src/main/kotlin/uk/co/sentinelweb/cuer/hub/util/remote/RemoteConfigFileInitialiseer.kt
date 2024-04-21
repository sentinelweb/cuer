package uk.co.sentinelweb.cuer.hub.util.remote

import java.io.File

class RemoteConfigFileInitialiseer {

    fun initIfNecessary() {
        println("Application Config Directory: $CONFIG_DIRECTORY")
        File(CONFIG_DIRECTORY)
            .takeIf { !it.exists() }
            ?.also {
                it.mkdirs()
                println("Created: ${it.absolutePath}")
            }
    }


    companion object {
        val CONFIG_DIRECTORY = System.getProperty("user.home") + "/.cuer"
    }
}