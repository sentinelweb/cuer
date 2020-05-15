package uk.co.sentinelweb.cuer.app.db.repository

import uk.co.sentinelweb.cuer.core.providers.CoroutineContextProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain

class PlaylistDatabaseRepository constructor(
    private val coProvider: CoroutineContextProvider,
    private val log: LogWrapper
) : DatabaseRepository<PlaylistDomain> {
    override suspend fun save(domain: PlaylistDomain): RepoResult<Boolean> =
        RepoResult.Data.Empty<Boolean>()


    override suspend fun save(domains: List<PlaylistDomain>): RepoResult<Boolean> =
        RepoResult.Data.Empty<Boolean>()


    override suspend fun load(id: Int): RepoResult<PlaylistDomain> =
        RepoResult.Data.Empty<PlaylistDomain>()


    override suspend fun loadList(filter: DatabaseRepository.Filter?): RepoResult<List<PlaylistDomain>> =
        listOf("Default", "Music", "Video", "News", "Philosophy", "Psychology", "Comedy")
            .map { PlaylistDomain(items = listOf(), title = it, id = it/* todo db key */) }
            .let { playlists -> RepoResult.Data(playlists) }


    override suspend fun count(filter: DatabaseRepository.Filter?): RepoResult<Int> =
        RepoResult.Data.Empty<Int>()


    override suspend fun delete(domain: PlaylistDomain): RepoResult<Boolean> =
        RepoResult.Data.Empty<Boolean>()

    class IdListFilter(val ids: List<Int>) : DatabaseRepository.Filter
}