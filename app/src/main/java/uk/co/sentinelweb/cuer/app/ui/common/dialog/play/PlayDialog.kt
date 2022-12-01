package uk.co.sentinelweb.cuer.app.ui.common.dialog.play

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.DialogPlayBinding
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogCreator
import uk.co.sentinelweb.cuer.app.ui.common.dialog.AlertDialogModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationRouter
import uk.co.sentinelweb.cuer.app.ui.main.MainContract
import uk.co.sentinelweb.cuer.app.ui.playlist.PlaylistItemMviContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemRowView
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerServiceManager
import uk.co.sentinelweb.cuer.app.usecase.PlayUseCase
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.PlatformLaunchWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayDialog constructor(
    private val f: Fragment,
    private val itemFactory: ItemFactory,
    private val itemModelMapper: ItemModelMapper,
    private val navigationRouter: NavigationRouter,
    private val castDialogWrapper: CastDialogWrapper,
    private val floatingService: FloatingPlayerServiceManager,
    private val log: LogWrapper,
    private val alertDialogCreator: AlertDialogCreator,
    private val youtubeApi: PlatformLaunchWrapper,
) : PlayUseCase.Dialog {
    init {
        log.tag(this)
    }

    private var _binding: DialogPlayBinding? = null
    private val binding: DialogPlayBinding
        get() = _binding ?: throw Exception("DialogPlayBinding not bound")

    override lateinit var playUseCase: PlayUseCase

    private lateinit var dialog: AlertDialog

    override fun showPlayDialog(item: PlaylistItemDomain?, playlistTitle: String?) {

        _binding = DialogPlayBinding.inflate(LayoutInflater.from(f.requireContext()))
        binding.dpLaunchYoutube.setOnClickListener {
            item?.apply { youtubeApi.launchVideoWithTimeSystem(media) }
            dialog.dismiss()
        }
        binding.dpChromecast.setOnClickListener {
            castDialogWrapper.showRouteSelector(f.childFragmentManager)
            item?.apply { playUseCase.setQueueItem(this) }
                ?: throw IllegalStateException("No item to play")
            dialog.dismiss()
        }
        binding.dpPortrait.setOnClickListener {
            dialog.dismiss()
            navigationRouter.navigate(
                NavigationModel(
                    NavigationModel.Target.LOCAL_PLAYER,
                    mapOf(NavigationModel.Param.PLAYLIST_ITEM to item)
                )
            )
        }
        binding.dpFullscreen.setOnClickListener {
            dialog.dismiss()
            navigationRouter.navigate(
                NavigationModel(
                    NavigationModel.Target.LOCAL_PLAYER_FULL,
                    mapOf(NavigationModel.Param.PLAYLIST_ITEM to item)
                )
            )
        }
        binding.dpFloating.setOnClickListener {
            val hasPermission = floatingService.hasPermission(f.requireActivity())
            if (hasPermission) {
                item?.apply { playUseCase.setQueueItem(this) }
                    ?: throw IllegalStateException("No item to play")
                // fixme the item is not received by the player mvi binding not made yet?
                floatingService.start(f.requireActivity(), item)
                playUseCase.attachControls(
                    (f.requireActivity() as? MainContract.View)?.playerControls
                )
                dialog.dismiss()
            } else {
                floatingService.requestPermission(f.requireActivity())
            }
        }
        item?.apply {
            val createView = itemFactory.createView(binding.dpItemLayout, false)
            binding.dpItemLayout.addView(createView as ItemRowView)
            itemFactory
                .createPresenter(createView, emptyInteractions)
                .update(
                    itemModelMapper.mapItem(
                        modelId = 0,
                        item = this,
                        index = 0,
                        canEdit = false,
                        canDelete = false,
                        canReorder = false,
                        playlistText = playlistTitle ?: "",
                        showOverflow = false,
                        deleteResources = null
                    ),
                    false
                )
        }
        dialog = MaterialAlertDialogBuilder(f.requireContext())
            .setTitle(f.getString(R.string.play_dialog_title))
            .setIcon(R.drawable.ic_play_black)
            .setView(binding.root)
            .create()
        dialog.show()
    }

    override fun showDialog(model: AlertDialogModel) {
        alertDialogCreator.create(model).show()
    }

    private val emptyInteractions = object : ItemContract.Interactions {
        override fun onClick(item: PlaylistItemMviContract.Model.Item) {}
        override fun onRightSwipe(item: PlaylistItemMviContract.Model.Item) {}
        override fun onLeftSwipe(item: PlaylistItemMviContract.Model.Item) {}
        override fun onPlay(item: PlaylistItemMviContract.Model.Item, external: Boolean) {}
        override fun onShowChannel(item: PlaylistItemMviContract.Model.Item) {}
        override fun onStar(item: PlaylistItemMviContract.Model.Item) {}
        override fun onRelated(item: PlaylistItemMviContract.Model.Item) {}
        override fun onShare(item: PlaylistItemMviContract.Model.Item) {}
        override fun onItemIconClick(item: PlaylistItemMviContract.Model.Item) {}
        override fun onPlayStartClick(item: PlaylistItemMviContract.Model.Item) {}
        override fun onGotoPlaylist(item: PlaylistItemMviContract.Model.Item) {}
    }
}