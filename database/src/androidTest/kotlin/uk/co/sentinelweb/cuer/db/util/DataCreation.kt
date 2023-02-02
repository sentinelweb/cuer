package uk.co.sentinelweb.cuer.db.util

import com.appmattus.kotlinfixture.Fixture
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.PlaylistItemDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class DataCreation(private val database: Database, private val fixture: Fixture, val timeProvider: TimeProvider) {
    private val guidCreator = GuidCreator()
    fun createPlaylistAndItem(): Pair<Playlist, Playlist_item> {
        val playlist = createPlaylist()
        val item = createPlaylistItem(playlist.id)
        return playlist to item
    }

    fun createPlaylist(): Playlist {
        val guid = guidCreator.create()
        val playlistInitial =
            fixture<Playlist>().copy(id = guid.value, parent_id = null, channel_id = null, image_id = null, thumb_id = null)
        database.playlistEntityQueries.create(playlistInitial)
        return playlistInitial
//        val playlistId = database.playlistEntityQueries.getInsertId().executeAsOne()
//        return playlistInitial.copy(id = playlistId)
    }

    fun createPlaylistItem(playlistId: String): Playlist_item {
        val guidChannel = guidCreator.create()
        val channel = fixture<Channel>().copy(id = guidChannel.value, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(channel)
        //val channelId = database.channelEntityQueries.getInsertId().executeAsOne()

        val guidMedia = guidCreator.create()
        val media = fixture<Media>().copy(id = guidMedia.value, channel_id = guidChannel.value, image_id = null, thumb_id = null)
        database.mediaEntityQueries.create(media)
        // val mediaId = database.mediaEntityQueries.getInsertId().executeAsOne()

        val guidItem = guidCreator.create()
        val item = fixture<Playlist_item>().copy(
            id = guidItem.value,
            media_id = guidMedia.value,
            playlist_id = playlistId,
            date_added = timeProvider.instant()
        )
        database.playlistItemEntityQueries.create(item)
        //val insertId = database.playlistItemEntityQueries.getInsertId().executeAsOne()
        //return initial.copy(id = insertId)
        return item
    }

    fun generatePlaylist(fixture: Fixture): PlaylistDomain {
        var fixPlaylist = fixture<PlaylistDomain>()
        while (fixPlaylist.items.size == 0) fixPlaylist = fixture()
        return fixPlaylist
    }

    fun fixturePlaylistItemList(): List<PlaylistItemDomain> {
        var list = fixture<List<PlaylistItemDomain>>()
        while (list.size == 0) list = fixture()
        return list
    }
}

fun PlaylistDomain.resetIds() = copy(
    id = null,
    channelData = channelData?.copy(id = null),
    parentId = null,
    thumb = thumb?.copy(id = null),
    image = image?.copy(id = null),
    items = items.map {
        it.copy(
            id = null,
            media = it.media.copy(
                id = null,
                thumbNail = it.media.thumbNail?.copy(id = null),
                image = it.media.image?.copy(id = null),
                channelData = it.media.channelData.copy(
                    id = null,
                    thumbNail = it.media.channelData.thumbNail?.copy(id = null),
                    image = it.media.channelData.image?.copy(id = null)
                )
            )
        )
    }
)