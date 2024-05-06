package uk.co.sentinelweb.cuer.tools.provider

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario

interface FragmentScenarioProvider<F : Fragment> {
    fun get(): FragmentScenario<F>
}