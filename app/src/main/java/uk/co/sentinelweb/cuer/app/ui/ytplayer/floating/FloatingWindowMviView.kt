package uk.co.sentinelweb.cuer.app.ui.ytplayer.floating

import com.arkivanov.mvikotlin.core.utils.diff
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.ViewRenderer
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.Command
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.MviStore.Label.PortraitPlayerOpen
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Event
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract.View.Model
import uk.co.sentinelweb.cuer.app.ui.ytplayer.AytViewHolder

class FloatingWindowMviView(
    private val service: FloatingPlayerService,
    private val aytViewHolder: AytViewHolder,
    private val windowManagement: FloatingWindowManagement,
) :
    BaseMviView<Model, Event>(),
    PlayerContract.View {

    init {
        aytViewHolder.addView(service.baseContext, windowManagement.binding!!.playerContainer, this)
    }

    override val renderer: ViewRenderer<Model> = diff {
        diff(get = Model::playState, set = {
            when (it) {
                else -> Unit
            }
        })
    }

    override suspend fun processLabel(label: PlayerContract.MviStore.Label) {
        when (label) {
            is Command -> label.command.let { aytViewHolder.processCommand(it) }
            is PortraitPlayerOpen -> label.also {
                aytViewHolder.switchView()
                //navMapper.navigate(NavigationModel(LOCAL_PLAYER, mapOf(PLAYLIST_ITEM to it.item)))
                // todo launch player activity - maybe need taskstak to put main behind
                service.cleanup()
            }
        }
    }

    fun cleanup() {
        aytViewHolder.switchView()
    }

}