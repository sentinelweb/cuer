package uk.co.sentinelweb.cuer.app.util.wrapper

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

class ContentUriUtil constructor(private val c: Context) {
    fun getFileName(uri: String) = DocumentFile.fromSingleUri(c, Uri.parse(uri))?.getName();
}