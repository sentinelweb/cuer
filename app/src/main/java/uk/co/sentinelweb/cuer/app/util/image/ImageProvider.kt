package uk.co.sentinelweb.cuer.app.util.image

import android.graphics.Bitmap
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseImageProvider

class ImageProvider(
    private val imageProvider: FirebaseImageProvider,
    private val imageFileRepo: ImageFileRepository
) {
    fun doLoad(requestManager: RequestManager, url: String) =
        if (url.startsWith("gs://")) requestManager.load(imageProvider.makeRef(url))
        else if (url.startsWith(ImageFileRepository.REPO_SCHEME_PREFIX)) requestManager.load(
            imageFileRepo.toLocalUri(url)
        )
        else requestManager.load(url)

    fun doLoad(requestBuilder: RequestBuilder<Bitmap>, url: String) =
        if (url.startsWith("gs://")) requestBuilder.load(imageProvider.makeRef(url))
        else if (url.startsWith(ImageFileRepository.REPO_SCHEME_PREFIX)) requestBuilder.load(
            imageFileRepo.toLocalUri(url)
        )
        else requestBuilder.load(url)

}