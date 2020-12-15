package uk.co.sentinelweb.cuer.domain.mutator

import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.LOOP
import uk.co.sentinelweb.cuer.domain.PlaylistDomain.PlaylistModeDomain.SINGLE
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain

class PlaylistMutator {
    // do i have to change the current index? yes
    // if from,to both before or after then ok
    // if from behind current and to after current decrement
    // if to before and from after then increment
    // todo update order
    fun moveItem(domain: PlaylistDomain, fromPosition: Int, toPosition: Int): PlaylistDomain {
        if (fromPosition != toPosition) {
            //Collections.swap(domain.items, fromPosition, toPosition)

            val items = domain.items.toMutableList()
            val movingItem = items.removeAt(fromPosition)
            items.add(/*if (fromPosition<toPosition) toPosition-1 else */toPosition, movingItem)
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

    // todo shuffle
    fun gotoPreviousItem(currentPlaylist: PlaylistDomain): PlaylistDomain {
        if (currentPlaylist.currentIndex > -1) {
            var newPosition = currentPlaylist.currentIndex
            newPosition--
            if (currentPlaylist.mode == LOOP && newPosition < 0) {
                newPosition = currentPlaylist.items.size - 1
            }
            return currentPlaylist.copy(currentIndex = newPosition)
        }
        return currentPlaylist
    }

    // todo shuffle
    fun gotoNextItem(currentPlaylist: PlaylistDomain): PlaylistDomain {
        var newPosition = currentPlaylist.currentIndex
        if (currentPlaylist.mode == LOOP || currentPlaylist.mode == SINGLE) {
            if (newPosition < currentPlaylist.items.size) {
                newPosition++
            }
            if (currentPlaylist.mode == LOOP &&
                newPosition >= currentPlaylist.items.size
            ) {
                newPosition = 0
            }
        }
        return currentPlaylist.copy(currentIndex = newPosition)
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
}