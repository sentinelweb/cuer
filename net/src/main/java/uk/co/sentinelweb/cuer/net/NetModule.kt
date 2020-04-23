package uk.co.sentinelweb.cuer.net

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.net.retrofit.RetrofitBuilder
import uk.co.sentinelweb.cuer.net.retrofit.ServiceType
import uk.co.sentinelweb.cuer.net.youtube.YoutubeVideosInteractor
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeVideoMediaDomainMapper
import uk.co.sentinelweb.cuer.net.youtube.videos.YoutubeVideosRetrofitInteractor

object NetModule {

    val netModule = module {
        single {RetrofitBuilder()}
        single (named(ServiceType.YOUTUBE)) {get<RetrofitBuilder>().buildYoutubeClient()}
        single {get<RetrofitBuilder>().buildYoutubeService(get(named(ServiceType.YOUTUBE)))}
        factory<YoutubeVideosInteractor> { YoutubeVideosRetrofitInteractor(get(), get(), YoutubeVideoMediaDomainMapper()) }
    }
}