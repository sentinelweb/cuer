package uk.co.sentinelweb.cuer.app.service.update

import uk.co.sentinelweb.cuer.domain.MediaDomain

class UpdateServiceContract {

    interface Controller {
        fun initialise()
        fun update()
    }

    interface Service {
        fun notify(mediaDomains: List<MediaDomain>)
    }

}