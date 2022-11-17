package uk.co.sentinelweb.cuer.net.pixabay

//import retrofit2.http.GET
//import retrofit2.http.Query
import uk.co.sentinelweb.cuer.net.client.ServiceExecutor
import uk.co.sentinelweb.cuer.net.pixabay.dto.PixabayImageListDto

internal class PixabayService(
    private val executor: ServiceExecutor
) {
    internal suspend fun imageSearch(
        query: String,
        imageType: String,
        orientation: String,
        perPage: Int = 50,
        key: String
    ): PixabayImageListDto = executor.get(
        path = "",
        urlParams = mapOf(
            "query" to query,
            "imageType" to imageType,
            "key" to key,
            "orientation" to orientation,
            "perPage" to perPage,
        )
    )
}
//internal interface PixabayService {
//    @GET("/api/")
//    suspend fun imageSearch(
//        @Query("q") query: String,
//        @Query("image_type") imageType: String,
//        @Query("orientation") orientation: String,
//        @Query("per_page") perPage: Int = 50,
//        @Query("key") key: String
//    ): PixabayImageListDto
//
//}