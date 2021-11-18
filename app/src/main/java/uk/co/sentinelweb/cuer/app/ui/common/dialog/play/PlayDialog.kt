package uk.co.sentinelweb.cuer.app.ui.common.dialog.play

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.co.sentinelweb.cuer.app.databinding.DialogPlayBinding
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemContract
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemFactory
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemModelMapper
import uk.co.sentinelweb.cuer.app.ui.playlist.item.ItemView
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlayDialog constructor(
    private val f: Fragment,
    private val itemFactory: ItemFactory,
    private val itemModelMapper: ItemModelMapper
) {
    private var _binding: DialogPlayBinding? = null
    private val binding: DialogPlayBinding
        get() = _binding ?: throw Exception("DialogPlayBinding view not bound")

    fun showPlayDialog(item: PlaylistItemDomain?) {
        _binding = DialogPlayBinding.inflate(LayoutInflater.from(f.requireContext()))
        binding.dpChromecast.setOnClickListener {}
        binding.dpPortrait.setOnClickListener {}
        binding.dpFullscreen.setOnClickListener {}
        binding.dpFloating.setOnClickListener {}
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
                        playlistText = "Playlist",
                        showOverflow = false
                    ),
                    false
                )
        }
        MaterialAlertDialogBuilder(f.requireContext())
            .setTitle("Play Item")
            .setView(binding.root)
            .create()
            .show()
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