package uk.co.sentinelweb.cuer.db.util

import com.appmattus.kotlinfixture.Fixture
import uk.co.sentinelweb.cuer.app.db.Database
import uk.co.sentinelweb.cuer.database.entity.Channel
import uk.co.sentinelweb.cuer.database.entity.Media
import uk.co.sentinelweb.cuer.database.entity.Playlist
import uk.co.sentinelweb.cuer.database.entity.Playlist_item
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class DataCreation(private val database: Database, private val fixture: Fixture) {
    fun createPlaylistAndItem(): Pair<Playlist, Playlist_item> {
        val playlist = createPlaylist()
        val item = createPlaylistItem(playlist.id)
        return playlist to item
    }

    fun createPlaylist(): Playlist {
        val playlistInitial =
            fixture<Playlist>().copy(id = 0, parent_id = null, channel_id = null, image_id = null, thumb_id = null)
        database.playlistEntityQueries.create(playlistInitial)
        val playlistId = database.playlistEntityQueries.getInsertId().executeAsOne()
        return playlistInitial.copy(id = playlistId)
    }

    fun createPlaylistItem(playlistId: Long): Playlist_item {
        val channel = fixture<Channel>().copy(id = 0, image_id = null, thumb_id = null)
        database.channelEntityQueries.create(channel)
        val channelId = database.channelEntityQueries.getInsertId().executeAsOne()
        val media = fixture<Media>().copy(id = 0, channel_id = channelId, image_id = null, thumb_id = null)
        database.mediaEntityQueries.create(media)
        val mediaId = database.mediaEntityQueries.getInsertId().executeAsOne()
        val initial = fixture<Playlist_item>().copy(id = 0, media_id = mediaId, playlist_id = playlistId)
        database.playlistItemEntityQueries.create(initial)
        val insertId = database.playlistItemEntityQueries.getInsertId().executeAsOne()
        return initial.copy(id = insertId)
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