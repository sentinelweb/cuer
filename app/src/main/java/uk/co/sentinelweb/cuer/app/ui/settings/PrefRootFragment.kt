package uk.co.sentinelweb.cuer.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.ui.main.MainActivity
import uk.co.sentinelweb.cuer.app.usecase.EmailUseCase
import uk.co.sentinelweb.cuer.app.usecase.ShareUseCase
import uk.co.sentinelweb.cuer.app.util.extension.fragmentScopeWithSource
import uk.co.sentinelweb.cuer.app.util.extension.linkScopeToActivity
import uk.co.sentinelweb.cuer.app.util.share.AndroidShareWrapper
import uk.co.sentinelweb.cuer.app.util.share.EmailWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.SnackbarWrapper
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import kotlin.random.Random

class PrefRootFragment : PreferenceFragmentCompat(), PrefRootContract.View, AndroidScopeComponent {

    override val scope: Scope by fragmentScopeWithSource<PrefRootFragment>()
    private val presenter: PrefRootContract.Presenter by inject()
    private val snackbarWrapper: SnackbarWrapper by inject()
    private val emailWrapper: EmailWrapper by inject()
    private val shareWrapper: AndroidShareWrapper by inject()
    private val buildConfig: BuildConfigDomain by inject()

    private val versionCategory
        get() = findPreference(R.string.prefs_root_version_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_version_key")
//    private val remoteServiceCategory
//        get() = findPreferenceCategory(R.string.prefs_root_remote_service_cat_key)
//            ?: throw IllegalArgumentException("Couldn't get: prefs_root_remote_service_cat_key")
//    private val remoteServicePreference
//        get() = findCheckbox(R.string.prefs_root_remote_service_key)
//            ?: throw IllegalArgumentException("Couldn't get: prefs_root_remote_service_key")

    private val feedbackPreference
        get() = findPreference(R.string.prefs_root_feedback_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_version_key")
    private val sharePreference
        get() = findPreference(R.string.prefs_root_share_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_version_key")
    private val debugPreference
        get() = findPreference(R.string.prefs_root_debug_cat_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_debug_key")
    private val onboardPreference
        get() = findPreference(R.string.prefs_root_onboard_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_onboard_key")
    private val bugReportPreference
        get() = findPreference(R.string.prefs_root_debug_send_reports_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_debug_send_reports_key")

    private val usabilityPreference
        get() = findPreference(R.string.prefs_root_usability_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_usability_key")

    private val bymcPreference
        get() = findPreference(R.string.prefs_root_usability_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_by_m_c_key")

    private val testPreference
        get() = findPreference(R.string.prefs_root_test_key)
            ?: throw IllegalArgumentException("Couldn't get: prefs_root_by_m_c_key")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        (layoutInflater.inflate(R.layout.settings_toolbar, container, false) as Toolbar).also {
            (view as ViewGroup).addView(it, 0)
            it.setupWithNavController(findNavController())
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        linkScopeToActivity()
    }

    override fun onStart() {
        super.onStart()
        presenter.initialisePrefs()
        (requireActivity() as? MainActivity)
            ?.takeIf { it.isPlayerShowing() }
            ?.also {
                view?.findViewById<FrameLayout>(android.R.id.list_container)
                    ?.setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.prefs_bottom_padding))
            }

        setPreferencesSummaries()
    }

    override fun setVersion(versionString: String) {
        versionCategory.summary = versionString
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_root, rootKey)
        //remoteServiceCategory.isVisible = buildConfig.cuerRemoteEnabled
        //remoteServicePreference.isVisible = buildConfig.cuerRemoteEnabled
        //debugPreference.isVisible = buildConfig.isDebug
        bugReportPreference.isVisible = buildConfig.isDebug
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.prefs_root_debug_send_reports_key) -> presenter.sendDebugReports()
            // getString(R.string.prefs_root_remote_service_key) -> presenter.toggleRemoteService()
            getString(R.string.prefs_root_feedback_key) -> presenter.onFeedback()
            getString(R.string.prefs_root_share_key) -> presenter.onShare()
            getString(R.string.prefs_root_onboard_key) -> presenter.resetOnboarding()
            getString(R.string.prefs_root_usability_key) -> presenter.launchUsability()
            getString(R.string.prefs_root_bymc_key) -> presenter.launchBymcDonate()
            getString(R.string.prefs_root_test_key) -> presenter.test()
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun launchLink(url: String) {
        shareWrapper.open(url)
    }

    override fun sendEmail(data: EmailUseCase.Data) {
        emailWrapper.launchEmail(data)
    }

    override fun launchShare(data: ShareUseCase.Data) {
        shareWrapper.share(data)
    }

    private fun setPreferencesSummaries() {
        feedbackPreference.summary = resources.getString(R.string.prefs_root_feedback_summary_how) + "\n\n" +
                resources.getStringArray(R.array.prefs_root_feedback_summary)
                    .let { it.get(Random.nextInt(it.size)) }
        sharePreference.summary =
            resources.getStringArray(R.array.prefs_root_share_summary)
                .let { it.get(Random.nextInt(it.size)) }
    }

//    override fun setRemoteServiceRunning(running: Boolean, address: String?) {
//        remoteServicePreference
//            .apply {
//                setChecked(running)
//                val summary =
//                    if (running)
//                        getString(R.string.prefs_root_remote_service_running) + ": " + address
//                    else
//                        getString(R.string.prefs_root_remote_service_not_running)
//                setSummary(summary)
//            }
//    }

    override fun showMessage(msg: String) {
        snackbarWrapper.make(msg).show()
    }
}