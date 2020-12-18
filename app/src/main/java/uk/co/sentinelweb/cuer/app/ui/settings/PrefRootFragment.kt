package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceFragmentCompat
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity

class PrefRootFragment constructor() : PreferenceFragmentCompat() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val onCreateView = super.onCreateView(inflater, container, savedInstanceState)
        (layoutInflater.inflate(R.layout.settings_toolbar, container, false) as Toolbar).also {
            (onCreateView as ViewGroup).addView(it, 0)
            //(activity as AppCompatActivity).setSupportActionBar(it)
            it.setupWithNavController(findNavController(), AppBarConfiguration(MainActivity.TOP_LEVEL_DESTINATIONS))
        }
        return onCreateView
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_root, rootKey)
    }

}