package uk.co.sentinelweb.cuer.app.ui.common.image

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.runBlocking
import uk.co.sentinelweb.cuer.app.db.repository.file.ImageFileRepository
import uk.co.sentinelweb.cuer.domain.ImageDomain
import java.io.File
import java.lang.IllegalStateException

class ImageSelectIntentCreator(
    private val a: AppCompatActivity
) {
    fun launchImageChooser(fragment: Fragment?) {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        fragment
            ?.apply {
                this.startActivityForResult(
                    Intent.createChooser(i, "Select Picture"),
                    REQUEST_CODE_SELECT_PICTURE
                )
            }
            ?: apply {
                a.startActivityForResult(
                    Intent.createChooser(i, "Select Picture"),
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
                    val fileUri = if ("content" == selectedImageUri.getScheme()) {
                        // copy data to cache
                        a.getContentResolver().openInputStream(selectedImageUri)
                            ?.readBytes()
                            ?.run {
                                val file = File(a.cacheDir, "user_image.jpg")
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
    }
}