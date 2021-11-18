package uk.co.sentinelweb.cuer.app.ui.common.dialog.play

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.databinding.DialogPlayBinding
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationMapper
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemView
import uk.co.sentinelweb.cuer.app.util.cast.CastDialogWrapper
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayDialog constructor(
    private val f: Fragment,
    private val itemFactory: ItemFactory,
    private val itemModelMapper: ItemModelMapper,
    private val navigationMapper: NavigationMapper,
    private val toastWrapper: ToastWrapper,
    private val castDialogWrapper: CastDialogWrapper
) {
    private var _binding: DialogPlayBinding? = null
    private val binding: DialogPlayBinding
        get() = _binding ?: throw Exception("DialogPlayBinding not bound")

    private lateinit var dialog: AlertDialog

    fun showPlayDialog(item: PlaylistItemDomain?, playlistTitle: String?) {
        _binding = DialogPlayBinding.inflate(LayoutInflater.from(f.requireContext()))
        binding.dpChromecast.setOnClickListener {
            castDialogWrapper.showRouteSelector(f.childFragmentManager)
            dialog.dismiss()
        }
        binding.dpPortrait.setOnClickListener {
            dialog.dismiss()
            navigationMapper.navigate(
                NavigationModel(
                    NavigationModel.Target.LOCAL_PLAYER,
                    mapOf(NavigationModel.Param.PLAYLIST_ITEM to item)
                )
            )

        }
        binding.dpFullscreen.setOnClickListener {
            dialog.dismiss()
            navigationMapper.navigate(
                NavigationModel(
                    NavigationModel.Target.LOCAL_PLAYER_FULL,
                    mapOf(NavigationModel.Param.PLAYLIST_ITEM to item)
                )
            )
        }
        binding.dpFloating.setOnClickListener {
            toastWrapper.show("Floating player")
            dialog.dismiss()
        }
        item?.apply {
            val createView = itemFactory.createView(binding.dpItemLayout)
            binding.dpItemLayout.addView(createView as ItemView)
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
                        showOverflow = false
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

    private val emptyInteractions = object : ItemContract.Interactions {
        override fun onClick(item: ItemContract.Model) {}
        override fun onRightSwipe(item: ItemContract.Model) {}
        override fun onLeftSwipe(item: ItemContract.Model) {}
        override fun onPlay(item: ItemContract.Model, external: Boolean) {}
        override fun onShowChannel(item: ItemContract.Model) {}
        override fun onStar(item: ItemContract.Model) {}
        override fun onRelated(item: ItemContract.Model) {}
        override fun onShare(item: ItemContract.Model) {}
        override fun onItemIconClick(item: ItemContract.Model) {}
        override fun onPlayStartClick(item: ItemContract.Model) {}
        override fun onGotoPlaylist(item: ItemContract.Model) {}
    }
}