package uk.co.sentinelweb.cuer.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity.Companion.TOP_LEVEL_DESTINATIONS
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.wrapper.StatusBarColorWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper

class PrefPlayerFragment : PreferenceFragmentCompat(), PrefPlayerContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PrefPlayerFragment>()
    private val presenter: PrefPlayerContract.Presenter by inject()
    private val log: LogWrapper by inject()
    private val statusBarColor: StatusBarColorWrapper by inject()

    init {
        log.tag(this)
    }

    private val playerFloatAutoCheckBox get() = findCheckbox(R.string.prefs_player_auto_float_key)
    private val playerRestartAfterUnlockCheckBox get() = findCheckbox(R.string.prefs_player_restart_after_unlock_key)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        (layoutInflater.inflate(R.layout.settings_toolbar, container, false) as Toolbar).also {
            (view as ViewGroup).addView(it, 0)
            //(activity as AppCompatActivity).setSupportActionBar(it)
            it.setupWithNavController(findNavController(), AppBarConfiguration(TOP_LEVEL_DESTINATIONS))
        }
        view.findViewById<FrameLayout>(android.R.id.list_container).setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.prefs_bottom_padding))
        statusBarColor.setStatusBarColorResource(R.color.primary_variant)
        return view
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_player, rootKey)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        playerFloatAutoCheckBox?.isChecked = presenter.playerAutoFloat
        playerRestartAfterUnlockCheckBox?.isChecked = presenter.restartAfterUnlock
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.prefs_player_auto_float_key) -> {
                presenter.playerAutoFloat = playerFloatAutoCheckBox?.isChecked ?: false
            }

            getString(R.string.prefs_player_restart_after_unlock_key) -> {
                presenter.restartAfterUnlock = playerRestartAfterUnlockCheckBox?.isChecked ?: false
            }

            else -> Unit
        }
        return super.onPreferenceTreeClick(preference)
    }
}
