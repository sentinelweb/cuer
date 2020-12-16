package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import uk.co.sentinelweb.cuer.app.R

class PrefRootFragment constructor() : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_root, rootKey)

    }
    
}