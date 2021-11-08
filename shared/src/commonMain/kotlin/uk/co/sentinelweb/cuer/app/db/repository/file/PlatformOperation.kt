package uk.co.sentinelweb.cuer.app.db.repository.file

import com.soywiz.korio.file.VfsFile

expect class PlatformOperation {
    fun delete(file: VfsFile)
}