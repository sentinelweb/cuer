package uk.co.sentinelweb.cuer.core.providers

import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import java.awt.GraphicsEnvironment

class PlayerConfigProviderJvm : PlayerConfigProvider {
    override fun invoke(): PlayerNodeDomain {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevices = ge.screenDevices
        val screens = screenDevices?.mapIndexed { index, device ->
            PlayerNodeDomain.Screen(
                index = index,
                width = device.displayMode.width,
                height = device.displayMode.height,
                refreshRate = device.displayMode.refreshRate,
                name = device.iDstring
            )
        }
        return PlayerNodeDomain(screens = screens ?: emptyList())
    }
}