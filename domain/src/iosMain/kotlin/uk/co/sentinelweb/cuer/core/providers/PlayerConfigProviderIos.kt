package uk.co.sentinelweb.cuer.core.providers

import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain

class PlayerConfigProviderIos {
    fun invoke(): PlayerNodeDomain = PlayerNodeDomain(emptyList())
}