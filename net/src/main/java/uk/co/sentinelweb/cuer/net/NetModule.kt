package uk.co.sentinelweb.cuer.net

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeChannelDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubePlaylistDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeRetrofitInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeVideoMediaDomainMapper

object NetModule {

    val netModule = module {
        single { RetrofitBuilder(get()) }
        single(named(ServiceType.YOUTUBE)) { get<RetrofitBuilder>().buildYoutubeClient() }
        single { get<RetrofitBuilder>().buildYoutubeService(get(named(ServiceType.YOUTUBE))) }
        factory<YoutubeInteractor> {
            YoutubeRetrofitInteractor(
                service = get(),
                keyProvider = get(),
                videoMapper = get(),
                channelMapper = get(),
                coContext = get(),
                errorMapper = get(),
                connectivity = get(),
                playlistMapper = get()
            )
        }
        factory { YoutubeVideoMediaDomainMapper(get()) }
        factory { YoutubeChannelDomainMapper(get()) }
        factory { YoutubePlaylistDomainMapper(get(), get()) }
        factory { ErrorMapper(log = get()) }
    }
}