package uk.co.sentinelweb.cuer.app.provider

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity

class ImageSelectIntentCreator(
    private val a: AppCompatActivity
) {
    fun launchImageChooser() {
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        a.startActivityForResult(Intent.createChooser(i, "Select Picture"), REQUEST_CODE_SELECT_PICTURE)
    }

    fun onResultReceived(
        requestCode: Int, resultCode: Int, data: Intent, callback: (String) -> Unit
    ) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PICTURE) {
                val selectedImageUri: Uri? = data.getData()
                if (null != selectedImageUri) {
                    callback(selectedImageUri.toString())
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_SELECT_PICTURE = 239837
    }
}