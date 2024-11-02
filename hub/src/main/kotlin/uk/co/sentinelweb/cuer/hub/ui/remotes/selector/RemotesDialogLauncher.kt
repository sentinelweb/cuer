package uk.co.sentinelweb.cuer.hub.ui.remotes.selector

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogContract
import uk.co.sentinelweb.cuer.app.ui.remotes.selector.RemotesDialogViewModel
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.NodeDomain
import uk.co.sentinelweb.cuer.domain.PlayerNodeDomain
import uk.co.sentinelweb.cuer.domain.RemoteNodeDomain
import uk.co.sentinelweb.cuer.hub.ui.remotes.selector.RemotesDialogLauncher.DisplayModel.Companion.Initial

class RemotesDialogLauncher(
    private val log: LogWrapper,
) : RemotesDialogContract.Launcher, KoinComponent {

    var modelObservable = MutableStateFlow(Initial)
        private set

    val viewModel: RemotesDialogViewModel by inject()

    init {
        log.tag(this)
    }

    data class DisplayModel(
        val isSelectRemotesVisible: Boolean,
        val unique: Double = Math.random(),
    ) {
        companion object {
            val Initial = DisplayModel(false)
        }
    }

    override fun launchRemotesDialog(
        callback: (NodeDomain, PlayerNodeDomain.Screen?) -> Unit,
        node: RemoteNodeDomain?,
        isSelectNodeOnly: Boolean
    ) {
        viewModel.resetState()
        viewModel.listener = callback
        if (isSelectNodeOnly) {
            viewModel.setSelectNodeOnly()
        } else {
            node?.also { viewModel.onNodeSelected(it) }
        }
        modelObservable.value = DisplayModel(true)
    }

    override fun hideRemotesDialog() {
        println("hideRemotesDialog()")
        modelObservable.value = DisplayModel(false)
    }

    companion object {
        @JvmStatic
        val launcherModule = module {
            factory {
                RemotesDialogViewModel(
                    remotesRepository = get(),
                    mapper = get(),
                    coroutines = get(),
                    playerInteractor = get(),
                    state = RemotesDialogContract.State(),
                    localRepository = get()
                )
            }
        }
    }
}
