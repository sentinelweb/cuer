package uk.co.sentinelweb.cuer.app.ui.main

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.common.navigation.DoneNavigation

class MainDoneNavigation(
    private val mainActivity: MainActivity
) : DoneNavigation {

    private val navController: NavController by lazy {
        val navHostFragment =
            mainActivity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }

    override fun navigateDone() {
        navController.popBackStack()
    }
}