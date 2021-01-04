package uk.co.sentinelweb.cuer.net.retrofit

//import retrofit2.converter.gson.GsonConverterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.youtube.YoutubeService
import java.util.concurrent.TimeUnit

class RetrofitBuilder constructor(
    private val config: NetModuleConfig
) {

    internal fun buildYoutubeClient() = Retrofit.Builder()
        .baseUrl(YOUTUBE_BASE)
        //.addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(
            Json(JsonConfiguration(ignoreUnknownKeys = true, isLenient = true)).asConverterFactory(
                CONTENT_TYPE
            )
        )
        .client(buildOkHttpClient())
        .build()

    fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        if (config.debug) {
            builder.addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    println(message)
                }
            }))
                .addInterceptor(PRINT_URL_INTERCEPTOR)
        }
        builder.readTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        builder.connectTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        builder.writeTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        builder.callTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        return builder.build()
    }

    internal fun buildYoutubeService(retrofit: Retrofit): YoutubeService =
        retrofit.create(YoutubeService::class.java)

    internal companion object {
        private const val YOUTUBE_BASE = "https://www.googleapis.com/youtube/v3/"
        private val CONTENT_TYPE = "application/json".toMediaType()

        val PRINT_URL_INTERCEPTOR = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                println(chain.request().url)
                return chain.proceed(chain.request())
            }
        }

    }

}