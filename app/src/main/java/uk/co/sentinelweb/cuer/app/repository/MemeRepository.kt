package uk.co.sentinelweb.cuer.app.repository

import uk.co.sentinelweb.cuer.app.domain.MediaDomain

interface MemeRepository {

    fun save(meme: MediaDomain)

    fun load(): List<MediaDomain>

    fun loadById(id: String): MediaDomain

    fun deleteById(id: String): Boolean

    fun delete(meme: MediaDomain): Boolean

    fun createMeme(name: String): MediaDomain

    fun getLastSavedId(): String?
}