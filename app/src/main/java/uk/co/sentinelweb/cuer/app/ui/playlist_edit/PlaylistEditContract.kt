package uk.co.sentinelweb.cuer.app.ui.playlist_edit

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.ui.common.validator.ValidatorModel
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

interface PlaylistEditContract {

    data class Model constructor(
        val titleDisplay: CharSequence,
        val titleEdit: CharSequence,
        val imageUrl: String?,
        val thumbUrl: String?,
        val starred: Boolean,
        val button: String?,
        val validation: ValidatorModel?
    )

    data class State constructor(
        var isCreate: Boolean = false,
        var model: Model? = null
    ) {
        lateinit var source: OrchestratorContract.Source
        lateinit var playlist: PlaylistDomain
    }

    companion object {

        @JvmStatic
        val fragmentModule = module {
            scope(named<PlaylistEditFragment>()) {
                viewModel {
                    PlaylistEditViewModel(
                        state = get(),
                        mapper = get(),
                        playlistRepo = get(),
                        log = get(),
                        imageProvider = get(),
                        prefsWrapper = get(named<GeneralPreferences>())
                    )
                }
                factory { State() }
                factory {
                    PlaylistEditModelMapper(
                        res = get(),
                        validator = get()
                    )
                }
                factory { PlaylistValidator(get()) }
            }
        }
    }
}