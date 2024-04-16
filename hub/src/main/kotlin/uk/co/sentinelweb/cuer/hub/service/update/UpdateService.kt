package uk.co.sentinelweb.cuer.hub.service.update

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.service.update.UpdateServiceContract

// placeholder for the actual update service
class UpdateService {
    companion object {
        val serviceModule = module {
            single<UpdateServiceContract.Manager> { UpdateServiceManager(get()) }
        }
    }
}