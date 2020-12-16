package uk.co.sentinelweb.cuer.app.db.backup.version

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ParserFactoryTest {
    private val sut = ParserFactory()

    @Before
    fun setUp() {
    }

    @Test
    fun create() {
    }

    @Test
    fun getVersion() {
        val data = """{ "version": 2,medias = [],playlists=[] }"""
        assertEquals(2, sut.getVersion(data))
    }

    @Test
    fun getVersion_1() {
        val data = """{ []}"""
        assertEquals(1, sut.getVersion(data))
    }
}