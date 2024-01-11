package uk.co.sentinelweb.cuer.remote.server

import kotlinx.coroutines.flow.Flow
import uk.co.sentinelweb.cuer.domain.LocalNodeDomain

interface LocalRepositoryContract {
    val node: Flow<LocalNodeDomain>
    fun getLocalNode(): LocalNodeDomain
}