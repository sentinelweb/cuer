package uk.co.sentinelweb.cuer.app.backup.version.v3

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import java.io.File
import java.net.URL

class V3ParserTest {

    private lateinit var fixtData: String

    private val log = SystemLogWrapper()
    private val sut = V3Parser(v4Mapper = V3ToV4Mapper())

    @Before
    fun setUp() {
        val url: URL? =
            this::class.java.classLoader?.getResource("v3_parser/database_v3.json")
        //log.d(url.toString())
        val inputPath = url.toString().substring("file:".length)
        fixtData = File(inputPath).readText()
        //log.d(fixtData.substring(0, 300))
    }

    @Test
    fun parse() {
        val data = sut.parse(fixtData)
        assertNotNull(data)
    }

    //    --- file -----
//     medias: 3986
//     items: 4055
//     playlists: 109
    @Test
    fun parseV3() {
        val data = sut.parseV3(fixtData)
        assertNotNull(data)
        data.also {
            log.d("--- file -----")
            log.d("medias: " + it.medias.size)
            log.d("items: " + it.playlists.fold(0) { acc, p -> acc + p.items.size })
            log.d("playlists: " + it.playlists.size)
        }
    }
}
