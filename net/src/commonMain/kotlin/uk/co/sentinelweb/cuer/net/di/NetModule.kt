package uk.co.sentinelweb.cuer.net.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.core.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.net.client.ErrorMapper
import uk.co.sentinelweb.cuer.net.client.KtorClientBuilder
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.client.ServiceType.*
import uk.co.sentinelweb.cuer.net.mappers.EscapeEntityMapper
import uk.co.sentinelweb.cuer.net.pixabay.PixabayInteractor
import uk.co.sentinelweb.cuer.net.pixabay.PixabayKtorInteractor
import uk.co.sentinelweb.cuer.net.pixabay.PixabayService
import uk.co.sentinelweb.cuer.net.pixabay.mapper.PixabayImageMapper
import uk.co.sentinelweb.cuer.net.remote.*
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeKtorInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.*

object NetModule {

    private val clientModule = module {
        factory { KtorClientBuilder() }
        factory(named(YOUTUBE)) { get<KtorClientBuilder>().build(get(), get()) }
        factory(named(PIXABAY)) { get<KtorClientBuilder>().build(get(), get()) }
        factory(named(REMOTE)) { get<KtorClientBuilder>().build(get(), get()) }
    }

    private val serviceModule = module {
        factory(named(YOUTUBE)) { ServiceExecutor(get(named(YOUTUBE)), YOUTUBE, get()) }
        factory { YoutubeService(executor = get(named(YOUTUBE))) }

        factory(named(PIXABAY)) { ServiceExecutor(get(named(PIXABAY)), PIXABAY, get()) }
        factory { PixabayService(executor = get(named(PIXABAY))) }

        factory(named(REMOTE)) { ServiceExecutor(get(named(REMOTE)), REMOTE, get()) }
        factory { RemoteStatusService(executor = get(named(REMOTE))) }
        factory { RemotePlaylistsService(executor = get(named(REMOTE))) }
        factory { RemotePlayerService(executor = get(named(REMOTE))) }
        factory { RemoteFilesService(executor = get(named(REMOTE))) }
    }

    private val interactorModule = module {
        factory<YoutubeInteractor> {
            YoutubeKtorInteractor(
                service = get(),
                keyProvider = get(named(YOUTUBE)),
                videoMapper = get(),
                channelMapper = get(),
                coContext = get(),
                errorMapper = get(),
                connectivity = get(),
                playlistMapper = get(),
                searchMapper = get()
            )
        }
        factory<PixabayInteractor> {
            PixabayKtorInteractor(
                service = get(),
                keyProvider = get(named(PIXABAY)),
                coContext = get(),
                imageMapper = get(),
                connectivity = get(),
                errorMapper = get()
            )
        }
        factory<RemoteStatusInteractor> {
            RemoteStatusKtorInteractor(
                availableService = get(),
                availableMessageMapper = get(),
                localRepository = get()
            )
        }
        factory<RemotePlaylistsInteractor> {
            RemotePlaylistsKtorInteractor(
                remotePlaylistsService = get()
            )
        }
        factory<RemotePlayerInteractor> {
            RemotePlayerKtorInteractor(
                service = get(),
                log = get()
            )
        }
        factory<RemoteFilesInteractor> {
            RemoteFilesKtorInteractor(
                remoteFilesService = get(),
            )
        }
    }

    private val mapperModule = module {
        factory { YoutubeVideoMediaDomainMapper(get(), get(), get()) }
        factory { YoutubeChannelDomainMapper(get(), get()) }
        factory { YoutubePlaylistDomainMapper(get(), get(), get(), get(), get()) }
        factory { YoutubeSearchMapper(get(), get(), get(), get(), get(), get()) }
        factory { YoutubeImageMapper() }

        factory { PixabayImageMapper() }

        factory { ErrorMapper(log = get()) }
        factory { TimeStampMapper(log = get()) }
        factory { EscapeEntityMapper() }
    }

    val modules = listOf(clientModule)
        .plus(interactorModule)
        .plus(mapperModule)
        .plus(serviceModule)

}