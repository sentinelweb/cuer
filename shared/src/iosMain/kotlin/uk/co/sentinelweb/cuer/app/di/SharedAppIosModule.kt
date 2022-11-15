package uk.co.sentinelweb.cuer.app.di

import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.factory.OrchestratorFactory
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.ChannelDomain
import uk.co.sentinelweb.cuer.domain.MediaDomain
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.SearchRemoteDomain
import uk.co.sentinelweb.cuer.net.NetResult
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePart

object SharedAppIosModule {

    private val factoryModule = module {
        single { OrchestratorFactory() }
    }

    private val utilModule = module {
        factory { AssetOperations() }
        factory<LogWrapper> { SystemLogWrapper() }// todo move to domain module?
    }

    private val netModule = module {
        factory<YoutubeInteractor> { DummyYoutubeInteractor() }
    }

    val modules = listOf(factoryModule)
        .plus(utilModule)
        .plus(netModule)

    // todo remove when net moved to kmm
    class DummyYoutubeInteractor : YoutubeInteractor {
        override suspend fun videos(ids: List<String>, parts: List<YoutubePart>): NetResult<List<MediaDomain>> =
            NetResult.Error(msg = "Not implemented", t = null)

        override suspend fun channels(ids: List<String>, parts: List<YoutubePart>): NetResult<List<ChannelDomain>> =
            NetResult.Error(msg = "Not implemented", t = null)

        override suspend fun playlist(id: String): NetResult<PlaylistDomain> =
            NetResult.Error(msg = "Not implemented", t = null)

        override suspend fun search(search: SearchRemoteDomain): NetResult<PlaylistDomain> =
            NetResult.Error(msg = "Not implemented", t = null)

    }
}