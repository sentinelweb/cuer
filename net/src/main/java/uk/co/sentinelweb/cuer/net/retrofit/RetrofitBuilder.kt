package uk.co.sentinelweb.cuer.net.retrofit

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService


internal class RetrofitBuilder constructor() {

    internal fun buildYoutubeClient() = Retrofit.Builder()
        .baseUrl(YOUTUBE_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .client(buildOkHttpClient())

        .build()

    private fun buildOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    println(message)
                }
            }))
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    println(chain.request().url)
                    return chain.proceed(chain.request())
                }
            })
            .build()
    }

    internal fun buildYoutubeService(retrofit: Retrofit): YoutubeService =
        retrofit.create(YoutubeService::class.java)

    internal companion object {
        private const val YOUTUBE_BASE = "https://www.googleapis.com/youtube/v3/"
    }

}