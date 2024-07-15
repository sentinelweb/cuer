package uk.co.sentinelweb.cuer.core.providers

import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain

interface PlayerConfigProvider {
    fun invoke(): PlayerNodeDomain
}