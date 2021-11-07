package uk.co.sentinelweb.cuer.net.pixabay.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PixabayImageListDto constructor(
    val total: Int,
    val totalHits: Int,
    val hits: List<ImageDto>
) {

    @Serializable
    internal data class ImageDto constructor(
        val webformatURL: String,
        val webformatWidth: Int,
        val webformatHeight: Int
    )

    internal enum class Type(val param: String) {
        ALL("all"),
        PHOTO("photo"),
        ILLUSTRATION("illustration"),
        VECTOR("vector")
    }

    internal enum class Orientation(val param: String) {
        ALL("all"),
        HORIZONTAL("horizontal"),
        VERTICAL("vertical")
    }
}