package uk.co.sentinelweb.cuer.app.db.repository.file

class ImageFileRepository(
    private val path:String,
) {

    fun saveImage(uri:String, data: ByteArray):String {
        return ""
    }

    fun loadImage(uri:String):ByteArray? = null

}