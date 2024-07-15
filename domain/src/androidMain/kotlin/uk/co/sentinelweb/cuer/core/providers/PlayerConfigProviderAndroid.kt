package uk.co.sentinelweb.cuer.core.providers

import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain

class PlayerConfigProviderAndroid(private val appContext: Context) : PlayerConfigProvider {
    override fun invoke(): PlayerNodeDomain {
        val displayManager = appContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays: List<Display> = displayManager.displays.asList()
        val screens = displays.mapIndexed() { i, display ->
            val size = Point()
            display.getSize(size)
            PlayerNodeDomain.Screen(
                index = i,
                width = size.x,
                height = size.y,
                name = display.name,
                refreshRate = display.refreshRate.toInt()
            )
        }
        return PlayerNodeDomain(screens)
    }
}
