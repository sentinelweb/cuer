package uk.co.sentinelweb.cuer.app.backup.version.v3

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.backup.version.v3.mapper.V3ToV4Mapper
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.DEFAULT_PLAYLIST_ID
import uk.co.sentinelweb.cuer.app.db.init.DatabaseInitializer.Companion.PHILOSOPHY_PLAYLIST_ID
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlaylistDomain
import uk.co.sentinelweb.cuer.domain.backup.BackupFileModel
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import java.io.File
import java.net.URL
import kotlin.test.assertEquals
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.BackupFileModel as BackupFileModelV3
import uk.co.sentinelweb.cuer.app.backup.version.v3.domain.PlaylistDomain as PlaylistDomainV3

class V3ParserTest {

    private lateinit var fixtData: String

    private val log = SystemLogWrapper()
    private val sut = V3Parser(v4Mapper = V3ToV4Mapper(GuidCreator()))

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
        val dataV3 = sut.parseV3(fixtData)
        assertNotNull(data)
        assertNotNull(dataV3)
        assertEquals(dataV3.medias.size, data.medias.size)
        assertEquals(dataV3.playlists.size, data.playlists.size)
        assertEquals(dataV3.playlists.countItems(), data.playlists.countItems())
        assertEquals(countChannelsV3(dataV3), countChannelsV4(data))

        log.d("parse() stats ")
        logStatsV3(dataV3)
        logStatsV4(data)
        data.playlists
            .find { it.id == DEFAULT_PLAYLIST_ID }
            ?.items?.size
            ?.also { log.d("default playlist size: $it") }
        data.playlists
            .find { it.id == PHILOSOPHY_PLAYLIST_ID }
            ?.items?.size
            ?.also { log.d("philosophy playlist size: $it") }
    }


    @Test
    fun parseV3() {
        val data = sut.parseV3(fixtData)
        assertNotNull(data)
    }

    @Test
    fun checkForDuplicatePlaylistItems() {
        val data = sut.parse(fixtData)
        val items = data.playlists.flatMap { it.items }
        val dupes = items.groupBy { it.media.platformId + "_" + it.playlistId }.filter { it.value.size > 1 }
        assertEquals(0, dupes.size)
    }

    //    --- file -----
//    Cuer:d: medias: 4003
//    Cuer:d: items: 4072
//    Cuer:d: playlists: 109
    private fun logStatsV3(data: BackupFileModelV3) {
        data.also {
            log.d("--- file v3 -----")
            log.d("channels: " + countChannelsV3(it))
            log.d("medias: " + it.medias.size)
            log.d("items: " + it.playlists.countItems())
            log.d("playlists: " + it.playlists.size)
        }
    }

    private fun logStatsV4(data: BackupFileModel) {
        data.also {
            log.d("--- file v4 -----")
            log.d("channels: " + countChannelsV4(it))
            log.d("medias: " + it.medias.size)
            log.d("items: " + it.playlists.fold(0) { acc, p -> acc + p.items.size })
            log.d("playlists: " + it.playlists.size)
        }
    }

    @JvmName("countItemsPlaylistDomain")
    private fun List<PlaylistDomain>.countItems() = this.fold(0) { acc, p -> acc + p.items.size }

    @JvmName("countItemsPlaylistDomainV3")
    private fun List<PlaylistDomainV3>.countItems() = this.fold(0) { acc, p -> acc + p.items.size }

    private fun countChannelsV4(it: BackupFileModel) =
        it.medias.map { it.channelData }
            .plus(it.playlists.mapNotNull { it.channelData })
            .distinct().size

    private fun countChannelsV3(it: BackupFileModelV3) =
        it.medias.map { it.channelData }
            .plus(it.playlists.mapNotNull { it.channelData })
            .distinct().size


}
