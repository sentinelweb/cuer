package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper

class PrefRootFragment constructor() : PreferenceFragmentCompat(), PrefRootContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource()
    private val presenter: PrefRootContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val onCreateView = super.onCreateView(inflater, container, savedInstanceState)
        (layoutInflater.inflate(R.layout.settings_toolbar, container, false) as Toolbar).also {
            (onCreateView as ViewGroup).addView(it, 0)
            it.setupWithNavController(findNavController())
        }
        return onCreateView
    }

    override fun onStart() {
        super.onStart()
        presenter.initialisePrefs()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_root, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.prefs_root_debug_send_reports_key) -> presenter.sendDebugReports()
            getString(R.string.prefs_root_remote_service_key) -> presenter.toggleRemoteService()
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun setRemoteServiceRunning(running: Boolean, address: String?) {
        (findPreference(getString(R.string.prefs_root_remote_service_key)) as CheckBoxPreference?)
            ?.apply {
                setChecked(running)
                val summary =
                    if (running)
                        getString(R.string.prefs_root_remote_service_running) + ": " + address
                    else
                        getString(R.string.prefs_root_remote_service_not_running)
                setSummary(summary)
            }
    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
    }
}