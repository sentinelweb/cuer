package uk.co.sentinelweb.cuer.app.ui.settings

import androidx.annotation.StringRes
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

fun PreferenceFragmentCompat.findCheckbox(@StringRes key: Int) =
    (findPreference(getString(key)) as? CheckBoxPreference)

fun PreferenceFragmentCompat.findPreference(@StringRes key: Int) =
    (findPreference(getString(key)) as? Preference)
