package uk.co.sentinelweb.cuer.app.usecase

import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.orchestrator.toIdentifier
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.*
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator

class GetFolderListUseCase(
    private val prefs: MultiPlatformPreferencesWrapper,
    private val fileOperations: PlatformFileOperation,
    private val guidCreator: GuidCreator,
    private val timeProvider: TimeProvider,
) {
    fun getFolderList(folderPath: String? = null): PlaylistDomain =
        (folderPath
            ?.let { AFile(it) }
            ?.takeIf { fileOperations.exists(it) }
            // todo return null here as no
            ?.let { fileOperations.list(it) }

            ?: prefs.folderRoots.map { AFile(it) })
            .mapNotNull { fileOperations.properties(it) }
            .let {
                val files = it.filter { it.isDirectory.not() }
                val subFolders = it.filter { it.isDirectory }
                val rootId = guidCreator.create().toIdentifier(MEMORY)
                PlaylistDomain(
                    id = rootId,
                    title = folderPath ?: "Top",
                    platform = PlatformDomain.FILESYSTEM,
                    platformId = folderPath,
                    items = files.mapIndexed { index, item ->
                        PlaylistItemDomain(
                            id = guidCreator.create().toIdentifier(MEMORY),
                            dateAdded = timeProvider.instant(),
                            order = index.toLong(),
                            playlistId = rootId,
                            media = MediaDomain(
                                id = guidCreator.create().toIdentifier(MEMORY),
                                title = item.name,
                                mediaType = MediaDomain.MediaTypeDomain.FILE,// todo scan
                                platform = PlatformDomain.FILESYSTEM,
                                platformId = item.file.path,
                                url = "file://${item.file.path}",
                                channelData = ChannelDomain(
                                    id = guidCreator.create().toIdentifier(MEMORY),
                                    platform = PlatformDomain.FILESYSTEM,
                                    platformId = folderPath
                                )
                            )
                        )
                    },
                    subPlaylists = subFolders.map {
                        PlaylistDomain(
                            id = guidCreator.create().toIdentifier(MEMORY),
                            title = it.name,
                            platform = PlatformDomain.FILESYSTEM,
                            platformId = it.file.path,
                        )
                    }
                )
            }
}
