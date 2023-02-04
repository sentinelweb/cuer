package uk.co.sentinelweb.cuer.app.backup.version.v3.mapper

import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.DEFAULT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.PHILOSOPHY_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Identifier
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.LOCAL
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.BackupFileModel as BackupFileModelV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.ChannelDomain as ChannelDomainV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.ImageDomain as ImageDomainV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.MediaDomain as MediaDomainV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.PlaylistDomain as PlaylistDomainV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.PlaylistItemDomain as PlaylistItemDomainV3

class V3ToV4Mapper(private val guidGenerator: GuidCreator) {

    fun map(v3Model: BackupFileModelV3): BackupFileModel {
        return BackupFileModel(
            4,
            v3Model.playlists.map { mapPlaylist(it) },
            v3Model.medias.map { mapMedia(it) },
        )
    }

    val playlistIdMap = mutableMapOf<Long, Identifier<GUID>>()
    fun mapPlaylist(v3: PlaylistDomainV3): PlaylistDomain =
        PlaylistDomain(
            id = playlistIdentifier(v3),
            title = v3.title,
            items = v3.items.map { mapPlaylistItem(it) },
            currentIndex = v3.currentIndex,
            parentId = v3.parentId?.let { playlistIdMap.getOrCreateNewId(it) },
            mode = v3.mode,
            type = v3.type,
            platform = v3.platform,
            channelData = v3.channelData?.let { mapChannel(it) },
            platformId = v3.platformId,
            starred = v3.starred,
            archived = v3.archived,
            default = v3.default,
            thumb = v3.thumb?.let { mapImage(it) },
            image = v3.image?.let { mapImage(it) },
            playItemsFromStart = v3.playItemsFromStart,
            config = v3.config,
        )

    private fun playlistIdentifier(v3: uk.co.sentinelweb.cuer.app.backup.version.v3.domain.PlaylistDomain) =
        if (v3.title.lowercase() == "default") {
            playlistIdMap.put(v3.id!!, DEFAULT_PLAYLIST_ID)
            DEFAULT_PLAYLIST_ID
        } else if (v3.title.lowercase() == "philosophy") {
            playlistIdMap.put(v3.id!!, PHILOSOPHY_PLAYLIST_ID)
            PHILOSOPHY_PLAYLIST_ID
        } else {
            playlistIdMap.getOrCreateNewId(v3.id!!)
        }

    val playlistItemIdMap = mutableMapOf<Long, Identifier<GUID>>()
    fun mapPlaylistItem(v3: PlaylistItemDomainV3): PlaylistItemDomain =
        PlaylistItemDomain(
            id = playlistItemIdMap.getOrCreateNewId(v3.id!!),
            media = mapMedia(v3.media),
            playlistId = playlistIdMap.getOrCreateNewId(v3.playlistId!!),
            dateAdded = v3.dateAdded,
            order = v3.order,
            archived = v3.archived
        )

    val mediaIdMap = mutableMapOf<Long, Identifier<GUID>>()
    fun mapMedia(v3: MediaDomainV3): MediaDomain =
        MediaDomain(
            id = mediaIdMap.getOrCreateNewId(v3.id!!),
            url = v3.url,
            platformId = v3.platformId,
            mediaType = v3.mediaType,
            platform = v3.platform,
            title = v3.title,
            duration = v3.duration,
            positon = v3.positon,
            dateLastPlayed = v3.dateLastPlayed,
            description = v3.description,
            published = v3.published,
            channelData = v3.channelData.let { mapChannel(it) },
            thumbNail = v3.thumbNail?.let { mapImage(it) },
            image = v3.image?.let { mapImage(it) },
            watched = v3.watched,
            starred = v3.starred,
            isLiveBroadcast = v3.isLiveBroadcast,
            isLiveBroadcastUpcoming = v3.isLiveBroadcastUpcoming,
            playFromStart = v3.playFromStart
        )

    val channelIdMap = mutableMapOf<Long, Identifier<GUID>>()
    fun mapChannel(v3: ChannelDomainV3): ChannelDomain =
        ChannelDomain(
            id = channelIdMap.getOrCreateNewId(v3.id!!),
            platformId = v3.platformId,
            platform = v3.platform,
            country = v3.country,
            title = v3.title,
            customUrl = v3.customUrl,
            description = v3.description,
            published = v3.published,
            thumbNail = v3.thumbNail?.let { mapImage(it) },
            image = v3.image?.let { mapImage(it) },
            starred = v3.starred,
        )

    val imageIdMap = mutableMapOf<Long, Identifier<GUID>>()
    fun mapImage(v3: ImageDomainV3): ImageDomain {
        return ImageDomain(
            id = imageIdMap.getOrCreateNewId(v3.id!!),
            url = v3.url,
            width = v3.width,
            height = v3.height,
        )
    }

    fun MutableMap<Long, Identifier<GUID>>.getOrCreateNewId(id: Long): Identifier<GUID> {
        return this[id] ?: guidGenerator.create().toIdentifier(LOCAL).also { this[id] = it }
    }
}