package uk.co.sentinelweb.cuer.app.ui.remotes

import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.resources.ActionResources
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract
import uk.co.sentinelweb.cuer.app.ui.onboarding.OnboardingContract.Config.Screen
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper

class RemotesHelpConfig(val res: ResourceWrapper) : OnboardingContract.ConfigBuilder {

    override fun build(): OnboardingContract.Config {
        return OnboardingContract.Config(
            screens = listOf(
                Screen(
                    title = ActionResources(
                        label = "Remotes",
                        icon = R.drawable.ic_wifi_tethering
                    ),
                    subtitle = res.getString(R.string.help_browse_desc),
                    lines = listOf(

                    )
                )
            )
        )
    }
}