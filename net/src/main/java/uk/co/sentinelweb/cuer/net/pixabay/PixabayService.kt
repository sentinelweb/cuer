package uk.co.sentinelweb.cuer.net.pixabay

import retrofit2.http.GET
import retrofit2.http.Query
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto

internal interface PixabayService {
    @GET("/api/")
    suspend fun imageSearch(
        @Query("q") query: String,
        @Query("image_type") imageType: String,
        @Query("orientation") orientation: String,
        @Query("per_page") perPage: Int = 50,
        @Query("key") key: String
    ): PixabayImageListDto

}