package uk.co.sentinelweb.cuer.domain.mutator

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.*
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistMutator {
    // do i have to change the current index? yes
    // if from,to both before or after then ok
    // if from behind current and to after current decrement
    // if to before and from after then increment
    // todo update order
    fun moveItem(domain: PlaylistDomain, fromPosition: Int, toPosition: Int): PlaylistDomain {
        if (fromPosition != toPosition) {
            val items = domain.items.toMutableList()
            val movingItem = items.removeAt(fromPosition)
            items.add(toPosition, movingItem)
            var newCurrentIndex = domain.currentIndex

            @Suppress("ConvertTwoComparisonsToRangeCheck")
            val newOrder =
                if (toPosition <= domain.currentIndex && fromPosition > domain.currentIndex) {
                    newCurrentIndex++
                    when (toPosition) {
                        0 -> items[1].order - 1000L
                        else -> (items[toPosition + 1].order + items[toPosition - 1].order) / 2L
                    }
                } else if (toPosition >= domain.currentIndex && fromPosition < domain.currentIndex) {
                    newCurrentIndex--
                    when (toPosition) {
                        items.size - 1 -> items[items.size - 2].order + 1000L
                        else -> (items[toPosition + 1].order + items[toPosition - 1].order) / 2L
                    }
                } else {
                    when (toPosition) {
                        0 -> items[1].order - 1000L
                        items.size - 1 -> items[items.size - 2].order + 1000L
                        else -> (items[toPosition + 1].order + items[toPosition - 1].order) / 2L
                    }
                }
            if (fromPosition == domain.currentIndex) {
                newCurrentIndex = toPosition
            }
            items.apply {
                set(toPosition, movingItem.copy(order = newOrder))
            }
            return domain.copy(items = items, currentIndex = newCurrentIndex)
        }
        return domain
    }


    fun gotoPreviousItem(playlist: PlaylistDomain): PlaylistDomain {
        var newPosition = playlist.currentIndex
        if (playlist.currentIndex > -1) {
            newPosition--
            if (playlist.mode == LOOP && newPosition < 0) {
                newPosition = playlist.items.size - 1
            }

        } else if (playlist.mode == SHUFFLE && playlist.items.size > 0) {
            newPosition = selectRandom(newPosition, playlist)
        }
        return playlist.copy(currentIndex = newPosition)
    }

    fun gotoNextItem(playlist: PlaylistDomain): PlaylistDomain {
        var newPosition = playlist.currentIndex
        if (playlist.mode == LOOP || playlist.mode == SINGLE) {
            if (newPosition < playlist.items.size) {
                newPosition++
            }
            if (playlist.mode == LOOP && newPosition >= playlist.items.size) {
                newPosition = 0
            }
        } else if (playlist.mode == SHUFFLE && playlist.items.size > 0) {
            newPosition = selectRandom(newPosition, playlist)
        }
        return playlist.copy(currentIndex = newPosition)
    }

    private fun selectRandom(oldIndex: Int, playlist: PlaylistDomain): Int {
        var newIndex = oldIndex
        while (newIndex == oldIndex && newIndex < playlist.items.size) {
            newIndex = (Math.random() * (playlist.items.size)).toInt()
        }
        return newIndex
    }

    fun playItem(
        playList: PlaylistDomain,
        playlistItem: PlaylistItemDomain
    ): PlaylistDomain {
        return playList.let {
            it.copy(
                currentIndex = it.items.indexOfFirst {
                    it.media.url == playlistItem.media.url
                }
            )
        }
    }

    fun delete(playlist: PlaylistDomain, deleteItem: PlaylistItemDomain): PlaylistDomain {
        return playlist.let { plist ->
            val newItems = plist.items.toMutableList()
            var newCurrentIndex = plist.currentIndex
            val deleteIndex = newItems.indexOfFirst { it.id == deleteItem.id }
            if (deleteIndex > -1) {
                newItems.removeAt(deleteIndex)
                newCurrentIndex = deleteIndex.let {
                    if (it < plist.currentIndex && it > 0) plist.currentIndex - 1 else plist.currentIndex
                }
                if (newCurrentIndex >= newItems.size) {
                    newCurrentIndex = newItems.size - 1
                }
            }
            plist.copy(
                items = newItems,
                currentIndex = newCurrentIndex
            )
        }
    }

    // todo  this needs not only to replace by id but also to synchronise the order and change the current index if necessary
    fun addOrReplaceItem(playlist: PlaylistDomain, item: PlaylistItemDomain) =
        if (item.playlistId == playlist.id) {
            val items = playlist.items.toMutableList()
            //val isReplace = items.removeIf { it.id == item.id }
            val removeIndex = items.indexOfFirst { it.id == item.id }
            if (removeIndex > -1) {
                items.removeAt(removeIndex)
            }
            var insertIndex = items.indexOfFirst { it.order > item.order }
            if (insertIndex == -1) insertIndex = items.size

            val newIndex = if (removeIndex == -1 && insertIndex <= playlist.currentIndex) {// add case
                playlist.currentIndex + 1
            } else if (removeIndex == -1 && insertIndex > playlist.currentIndex) {// add case
                playlist.currentIndex
            } else if (removeIndex == playlist.currentIndex) {// move current item
                insertIndex
            } else if (removeIndex < playlist.currentIndex && insertIndex >= playlist.currentIndex) {
                playlist.currentIndex - 1
            } else if (removeIndex > playlist.currentIndex && insertIndex <= playlist.currentIndex) {
                playlist.currentIndex + 1
            } else playlist.currentIndex

            items.add(insertIndex, item)
            playlist.copy(
                items = items,
                currentIndex = newIndex
            )
        } else throw IllegalStateException("Item is not on this playlist")

    fun remove(playlist: PlaylistDomain, plistItem: PlaylistItemDomain): PlaylistDomain =
        if (playlist.id == plistItem.playlistId) {
            playlist.items.indexOfFirst { plistItem.id == it.id }
                .takeIf { it != -1 }
                ?.let { index ->
                    val newIndex = if (index < playlist.currentIndex) {
                        playlist.currentIndex - 1
                    } else playlist.currentIndex
                    val toMutableList = playlist.items.toMutableList()
                    toMutableList.removeAt(index)
                    playlist.copy(currentIndex = newIndex, items = toMutableList)
                } ?: playlist
        } else playlist

}