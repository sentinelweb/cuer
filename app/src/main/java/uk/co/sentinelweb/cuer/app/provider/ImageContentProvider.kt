package uk.co.sentinelweb.cuer.app.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor

class ImageContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        //TODO("Implement this to handle requests to delete one or more rows")
        return 0
    }

    override fun getType(uri: Uri): String? {
//        TODO("Implement this to handle requests for the MIME type of the data" +
//                "at the given URI")
        return ""
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
//        TODO("Implement this to handle requests to insert a new row.")
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                              selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
//        TODO("Implement this to handle query requests from clients.")
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                               selectionArgs: Array<String>?): Int {
//        TODO("Implement this to handle requests to update one or more rows.")
        return 0
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return super.openFile(uri, mode)
    }

}