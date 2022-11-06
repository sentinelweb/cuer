package uk.co.sentinelweb.cuer.net

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.net.mappers.TimeStampMapper
import uk.co.sentinelweb.cuer.net.pixabay.PixabayInteractor
import uk.co.sentinelweb.cuer.net.pixabay.PixabayRetrofitInteractor
import uk.co.sentinelweb.cuer.net.pixabay.mapper.PixabayImageMapper
import uk.co.sentinelweb.cuer.net.retrofit.ErrorMapper
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType.PIXABAY
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType.YOUTUBE
import uk.co.sentinelweb.cuer.net.youtube.YoutubeInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeRetrofitInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.mapper.*

object NetModule {

    val netModule = module {
        single { RetrofitBuilder(get(), get()) }
        single(named(YOUTUBE)) { get<RetrofitBuilder>().buildYoutubeClient() }
        single { get<RetrofitBuilder>().buildYoutubeService(get(named(YOUTUBE))) }
        factory<YoutubeInteractor> {
            YoutubeRetrofitInteractor(
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
        factory { YoutubeVideoMediaDomainMapper(get(), get()) }
        factory { YoutubeChannelDomainMapper(get(), get()) }
        factory { YoutubePlaylistDomainMapper(get(), get(), get(), get(), get()) }
        factory { YoutubeSearchMapper(get(), get(), get(), get(), get()) }
        factory { YoutubeImageMapper() }

        single(named(PIXABAY)) { get<RetrofitBuilder>().buildPixabayClient() }
        single { get<RetrofitBuilder>().buildPixabayService(get(named(PIXABAY))) }
        factory<PixabayInteractor> {
            PixabayRetrofitInteractor(
                service = get(),
                keyProvider = get(named(PIXABAY)),
                coContext = get(),
                imageMapper = get(),
                connectivity = get(),
                errorMapper = get()
            )
        }
        factory { PixabayImageMapper() }

        factory { ErrorMapper(log = get()) }

        factory { TimeStampMapper(log = get()) }
    }
}