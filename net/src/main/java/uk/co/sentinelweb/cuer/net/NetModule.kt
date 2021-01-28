package uk.co.sentinelweb.cuer.net

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeRetrofitInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeChannelDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeImageMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubePlaylistDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.YoutubeVideoMediaDomainMapper

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
        factory { YoutubeVideoMediaDomainMapper(get(), get()) }
        factory { YoutubeChannelDomainMapper(get(), get()) }
        factory { YoutubePlaylistDomainMapper(get(), get(), get(), get()) }
        factory { YoutubeImageMapper() }
        factory { ErrorMapper(log = get()) }
    }
}