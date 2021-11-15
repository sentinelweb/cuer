package uk.co.sentinelweb.cuer.app.util.image

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.runBlocking
import uk.co.sentinelweb.cuer.app.R
import uk.co.sentinelweb.cuer.app.util.wrapper.ResourceWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain
import java.io.File

class ImageSelectIntentHandler(
    private val a: AppCompatActivity,
    private val res: ResourceWrapper,
    private val bitmapSizer: BitmapSizer
) {
    fun launchImageChooser(fragment: Fragment?) {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        fragment
            ?.apply {
                this.startActivityForResult(
                    Intent.createChooser(i, res.getString(R.string.title_select_picture)),
                    REQUEST_CODE_SELECT_PICTURE
                )
            }
            ?: apply {
                a.startActivityForResult(
                    Intent.createChooser(i, res.getString(R.string.title_select_picture)),
                    REQUEST_CODE_SELECT_PICTURE
                )
            }
    }

    fun onResultReceived(
        requestCode: Int, resultCode: Int, data: Intent, callback: (ImageDomain) -> Unit
    ) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PICTURE) {
                val selectedImageUri: Uri? = data.getData()
                if (null != selectedImageUri) {
                    Log.d("ImageSelectIntentHandler", selectedImageUri.toString())
                    val fileUri = if ("content" == selectedImageUri.getScheme()) {
                        // copy data to cache
                        a.getContentResolver().openInputStream(selectedImageUri)
                            ?.readBytes()
                            ?.let {
                                val maxDim = res.getDimensionPixelSize(R.dimen.max_cache_image_size)
                                bitmapSizer.checkSize(it, maxDim)
                            }
                            ?.run {
                                val file = File(a.cacheDir, IMAGE_NAME)
                                file.writeBytes(this)
                                Uri.fromFile(file)
                            }
                    } else if ("file" == selectedImageUri.getScheme()) {
                        selectedImageUri
                    } else throw IllegalStateException("scheme not supported: $selectedImageUri")
                    runBlocking {
                        callback(
                            ImageDomain(fileUri.toString())
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SELECT_PICTURE = 239837
        const val IMAGE_NAME = "user_image.png"
    }
}