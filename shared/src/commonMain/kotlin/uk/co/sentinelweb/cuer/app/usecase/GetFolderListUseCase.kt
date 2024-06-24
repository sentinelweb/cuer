package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.AFileProperties
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain.MediaTypeDomain.*
import uk.co.sentinelweb.cuer.domain.PlatformDomain.FILESYSTEM
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class GetFolderListUseCase(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val fileOperations: PlatformFileOperation,
    private val guidCreator: GuidCreator,
    private val timeProvider: TimeProvider,
    private val log: LogWrapper
) {
    init {
        log.tag(this)
    }

    fun getFolderList(folderPath: String? = null): PlaylistAndChildrenDomain? =
        (if (folderPath == null) {
            prefs.folderRoots.map { AFile(it) }
        } else {
            folderPath
                .let { truncatedToFullFolderPath(it) }
                ?.takeIf { checkFolderPathIsInAllowedSet(it) }
                ?.let { AFile(it) }
                ?.takeIf { fileOperations.exists(it) }
                ?.let { fileOperations.list(it) }
        })
            ?.also { log.d("folderPath: $folderPath") }
            ?.mapNotNull { fileOperations.properties(it) }
            ?.sortedBy { it.name }
            ?.let {
                val fullFolderPath = folderPath
                    ?.let { truncatedToFullFolderPath(it) }
                if (folderPath != null && fullFolderPath == null) {
                    throw IllegalArgumentException("folderPath is not configured")
                }
                log.d("fullFolderPath: $fullFolderPath")
                val filesProperties = it.filter { it.isDirectory.not() }
                val subFoldersProperties = it.filter { it.isDirectory }
                val rootId = guidCreator.create().toIdentifier(MEMORY)
                val subFolders = subFoldersProperties.map {
                    PlaylistDomain(
                        id = guidCreator.create().toIdentifier(MEMORY),
                        title = it.name,
                        platform = FILESYSTEM,
                        platformId = fullToTruncatedFolderPath(it.file.path),
                        parentId = rootId
                    )
                }.toMutableList()

                // generate parent folder link
                if (folderPath != null) {
                    val parent = if (prefs.folderRoots.contains(fullFolderPath)) {
                        null
                    } else {
                        fileOperations.parent(
                            AFile(
                                fullFolderPath
                                    ?: throw IllegalStateException("fullFolderPath can't be null here")
                            )
                        )
                    }
                    PlaylistDomain(
                        id = guidCreator.create().toIdentifier(MEMORY),
                        title = "..",
                        platform = FILESYSTEM,
                        platformId = parent?.path?.let { fullToTruncatedFolderPath(it) },
                        parentId = rootId
                    ).apply { subFolders.add(0, this) }
                }
                val folderProperties = fullFolderPath?.let { fileOperations.properties(AFile(it)) }
                val playlist = PlaylistDomain(
                    id = rootId,
                    title = folderProperties?.name ?: "Top",
                    platform = FILESYSTEM,
                    platformId = folderPath,
                    items = fullFolderPath?.let {
                        filesProperties.mapIndexed { index, item ->
                            mapFileToPlaylist(index, rootId, item, it)
                        }
                    } ?: listOf()
                )

                PlaylistAndChildrenDomain(
                    playlist = playlist,
                    children = subFolders
                )
            }

    internal fun checkFolderPathIsInAllowedSet(folderPath: String): Boolean =
        prefs.folderRoots.any { folderPath.startsWith(it) }

    internal fun fullToTruncatedFolderPath(path: String): String? =
        prefs.folderRoots
            .find { path.startsWith(it) }
            ?.let { path.replace(it, it.substringAfterLast("/")) }

    fun truncatedToFullFolderPath(path: String): String? =
        prefs.folderRoots
            .find { path.startsWith(it.substringAfterLast("/")) }
            ?.let { path.replace(it.substringAfterLast("/"), it) }

    private fun mapFileToPlaylist(
        index: Int,
        playlistId: OrchestratorContract.Identifier<GUID>,
        item: AFileProperties,
        folderPath: String?
    ): PlaylistItemDomain {
        val truncatedPath = fullToTruncatedFolderPath(item.file.path)!!
        return PlaylistItemDomain(
            id = guidCreator.create().toIdentifier(MEMORY),
            dateAdded = timeProvider.instant(),
            order = index.toLong(),
            playlistId = playlistId,
            media = MediaDomain(
                id = guidCreator.create().toIdentifier(MEMORY),
                title = item.name,
                mediaType = checkMediaType(item),
                platform = FILESYSTEM,
                platformId = truncatedPath,
                url = "file://${truncatedPath}",
                channelData = ChannelDomain(
                    id = guidCreator.create().toIdentifier(MEMORY),
                    platform = FILESYSTEM,
                    platformId = folderPath
                )
            )
        )
    }

    private fun checkMediaType(item: AFileProperties): MediaTypeDomain {
        val ext = getFileExtension(item.name)
        return mediaTypes[ext]?.type ?: FILE
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast(".", "")
    }

    data class MediaDetail(val type: MediaTypeDomain, val mimetype: String)

    companion object {
        val mediaTypes: Map<String, MediaDetail> = mapOf(
            "mp4" to MediaDetail(VIDEO, "video/mp4"),
            "m4v" to MediaDetail(VIDEO, "video/x-m4v"),
            "mkv" to MediaDetail(VIDEO, "video/x-matroska"),
            "flv" to MediaDetail(VIDEO, "video/x-flv"),
            "f4v" to MediaDetail(VIDEO, "video/x-f4v"),
            "avi" to MediaDetail(VIDEO, "video/vnd.avi"),
            "mov" to MediaDetail(VIDEO, "video/quicktime"),
            "qt" to MediaDetail(VIDEO, "video/quicktime"),
            "wmv" to MediaDetail(VIDEO, "video/x-ms-wmv"),
            "rm" to MediaDetail(VIDEO, "application/vnd.rn-realmedia"),
            "rmvb" to MediaDetail(VIDEO, "application/vnd.rn-realmedia-vbr"),
            "asf" to MediaDetail(VIDEO, "video/x-ms-asf"),
            "amv" to MediaDetail(VIDEO, "video/x-amv"),
            "mpg" to MediaDetail(VIDEO, "video/mpeg"),
            "mpeg" to MediaDetail(VIDEO, "video/mpeg"),
            "m2v" to MediaDetail(VIDEO, "video/mpeg"),
            "svi" to MediaDetail(VIDEO, "video/svi"),
            "3gp" to MediaDetail(VIDEO, "video/3gpp"),
            "mxf" to MediaDetail(VIDEO, "application/mxf"),
            "roq" to MediaDetail(VIDEO, "video/x-flic"),
            "vob" to MediaDetail(VIDEO, "video/vnd.dvd.video"),
            "ogg" to MediaDetail(VIDEO, "video/ogg"),
            "mp3" to MediaDetail(AUDIO, "audio/mpeg"),
            "aac" to MediaDetail(AUDIO, "audio/aac"),
            "flac" to MediaDetail(AUDIO, "audio/flac"),
            "m4a" to MediaDetail(AUDIO, "audio/mp4"),
            "ogg" to MediaDetail(AUDIO, "audio/ogg"),
            "wav" to MediaDetail(AUDIO, "audio/vnd.wav"),
            "wma" to MediaDetail(AUDIO, "audio/x-ms-wma"),
            "webm" to MediaDetail(AUDIO, "audio/webm"),
            "opus" to MediaDetail(AUDIO, "audio/ogg"),
            "3ga" to MediaDetail(AUDIO, "audio/3gpp")
        )
    }
}
