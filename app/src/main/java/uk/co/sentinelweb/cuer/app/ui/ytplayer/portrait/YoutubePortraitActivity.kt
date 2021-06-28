package uk.co.sentinelweb.cuer.app.ui.ytplayer.portrait

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.core.view.BaseMviView
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.core.scope.Scope
import uk.co.sentinelweb.cuer.app.databinding.ActivityYoutubePortraitBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.player.PlayerController
import uk.co.sentinelweb.cuer.app.util.extension.activityScopeWithSource
import uk.co.sentinelweb.cuer.app.util.wrapper.EdgeToEdgeWrapper
import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.ext.serialise

class YoutubePortraitActivity : AppCompatActivity(),
    AndroidScopeComponent {

    override val scope: Scope by activityScopeWithSource()

    private val controller: PlayerController by inject()
    private val log: LogWrapper by inject()
    private val coroutines: CoroutineContextProvider by inject()
    private val edgeToEdgeWrapper: EdgeToEdgeWrapper by inject()

    lateinit var mviView: YoutubePortraitActivity.MviViewImpl
    private lateinit var binding: ActivityYoutubePortraitBinding

    init {
        log.tag(this)
        log.d("YoutubePortraitActivity.init")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYoutubePortraitBinding.inflate(layoutInflater)
        log.d("YoutubePortraitActivity.onCreate")
        setContentView(binding.root)
        edgeToEdgeWrapper.setDecorFitsSystemWindows(this)
    }

    // region MVI view
    inner class MviViewImpl constructor() :
        BaseMviView<PlayerContract.View.Model, PlayerContract.View.Event>(),
        PlayerContract.View {

        init {

        }

        override fun render(model: PlayerContract.View.Model) {

        }

        override suspend fun processLabel(label: PlayerContract.MviStore.Label) {

        }
    }

    companion object {

        fun start(c: Context, playlistItem: PlaylistItemDomain) = c.startActivity(
            Intent(c, YoutubePortraitActivity::class.java).apply {
                putExtra(NavigationModel.Param.PLAYLIST_ITEM.toString(), playlistItem.serialise())
            })
    }

}