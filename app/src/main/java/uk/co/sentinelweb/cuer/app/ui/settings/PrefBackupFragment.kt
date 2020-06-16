package uk.co.sentinelweb.cuer.app.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.scope.currentScope
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.R

class PrefBackupFragment constructor() : PreferenceFragmentCompat(), PrefBackupContract.View {

    private val presenter: PrefBackupContract.Presenter by currentScope.inject()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_backup, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_backup) -> presenter.backupDatabaseToJson()
        }

        return super.onPreferenceTreeClick(preference)
    }

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PrefBackupFragment>()) {
                scoped<PrefBackupContract.View> { getSource() }
                scoped<PrefBackupContract.Presenter> {
                    PrefBackupPresenter(
                        view = get(),
                        toastWrapper = get()
                    )
                }
            }
        }
    }
}