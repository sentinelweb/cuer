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
}