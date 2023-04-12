package uk.co.sentinelweb.cuer.app.service.update

import uk.co.sentinelweb.cuer.domain.MediaDomain

class UpdateServiceContract {

    interface Controller {
        fun initialise()
        fun update()
        fun destroy()
        fun handleAction(action: String?)
    }

    interface Service {
        fun notify(mediaDomains: List<MediaDomain>)
    }

    interface Manager {
        fun start()
        fun stop()
        fun getService(): Service?
        fun isRunning(): Boolean
    }

}