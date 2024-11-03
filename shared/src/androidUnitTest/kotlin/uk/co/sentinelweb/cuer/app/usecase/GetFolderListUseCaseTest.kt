package uk.co.sentinelweb.cuer.app.usecase

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.AFileProperties
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.usecase.GetFolderListUseCase.Companion.PARENT_FOLDER_TEXT
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.domain.PlatformDomain.FILESYSTEM
import uk.co.sentinelweb.cuer.domain.PlaylistAndChildrenDomain
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import kotlin.test.assertEquals

class GetFolderListUseCaseTest {

    private val prefs: MultiPlatformPreferencesWrapper = mockk(relaxed = true)
    private val fileOperations: PlatformFileOperation = mockk(relaxed = true)
    private val guidCreator: GuidCreator = GuidCreator()
    private val timeProvider: TimeProvider = mockk(relaxed = true)
    private val log: LogWrapper = SystemLogWrapper()

    private lateinit var sut: GetFolderListUseCase

    @Before
    fun setUp() {
        sut = GetFolderListUseCase(prefs, fileOperations, guidCreator, timeProvider, log)
    }

    @Test
    fun getFolderList_null_returns_root_folders() {
        val folderPath: String? = null
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        every { fileOperations.properties(AFile("/path1")) } returns AFileProperties(
            file = AFile("/path1"),
            name = "path1",
            size = 0,
            isDirectory = true
        )
        every { fileOperations.properties(AFile("/x/y/z/path2")) } returns AFileProperties(
            file = AFile("/x/y/z/path2"),
            name = "path2",
            size = 0,
            isDirectory = true
        )
        every { fileOperations.properties(AFile("/a/path3")) } returns AFileProperties(
            file = AFile("/a/path3"),
            name = "path3",
            size = 0,
            isDirectory = true
        )
        val actual = sut.getFolderList(null)

        assertNotNull(actual!!)

        assertEquals("Top", actual.playlist.title)
        assertEquals(FILESYSTEM, actual.playlist.platform)
        assertNull(actual.playlist.platformId)
        assertEquals(0, actual.playlist.items.size) // no top level folder
        assertEquals(3, actual.children.size)

        assertNotEquals(PARENT_FOLDER_TEXT, actual.children[0].title)

        assertEquals("path1", actual.children[0].title)
        assertEquals(FILESYSTEM, actual.children[0].platform)
        assertEquals("path1", actual.children[0].platformId)

        assertEquals("path2", actual.children[1].title)
        assertEquals(FILESYSTEM, actual.children[1].platform)
        assertEquals("path2", actual.children[1].platformId)

        assertEquals("path3", actual.children[2].title)
        assertEquals(FILESYSTEM, actual.children[2].platform)
        assertEquals("path3", actual.children[2].platformId)
    }

    @Test
    fun getFolderList_top_level_folder() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        every { fileOperations.exists(AFile("/path1")) } returns true
        every { fileOperations.list(AFile("/path1")) } returns listOf(
            AFile("/path1/file1.mp3"),
            AFile("/path1/file2.mkv")
        )
        every { fileOperations.properties(AFile("/path1")) } returns AFileProperties(
            file = AFile("/path1"),
            name = "path1",
            size = 0,
            isDirectory = true
        )
        every { fileOperations.properties(AFile("/path1/file1.mp3")) } returns AFileProperties(
            file = AFile("/path1/file1.mp3"),
            name = "file1.mp3",
            size = 2000,
            isDirectory = false
        )
        every { fileOperations.properties(AFile("/path1/file2.mkv")) } returns AFileProperties(
            file = AFile("/path1/file2.mkv"),
            name = "file2.mkv",
            size = 3000,
            isDirectory = false
        )
        val actual: PlaylistAndChildrenDomain? = sut.getFolderList("path1")
        log.d(actual.toString())
        assertNotNull(actual!!)
        assertEquals("path1", actual.playlist.title)
        assertEquals(FILESYSTEM, actual.playlist.platform)
        assertEquals("path1", actual.playlist.platformId)
        assertEquals(1, actual.children.size) // has parent folder link
        assertEquals(2, actual.playlist.items.size)
        // verify parent folder link
        assertEquals(PARENT_FOLDER_TEXT, actual.children[0].title)
        assertEquals(FILESYSTEM, actual.children[0].platform)
        assertNull(actual.children[0].platformId)

        assertEquals("file1.mp3", actual.playlist.items[0].media.title)
        assertEquals(FILESYSTEM, actual.playlist.items[0].media.platform)
        assertEquals("path1/file1.mp3", actual.playlist.items[0].media.platformId)

        assertEquals("file2.mkv", actual.playlist.items[1].media.title)
        assertEquals(FILESYSTEM, actual.playlist.items[1].media.platform)
        assertEquals("path1/file2.mkv", actual.playlist.items[1].media.platformId)
    }

    @Test
    fun getFolderList_2nd_level_folder() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        every { fileOperations.exists(AFile("/path1")) } returns true
        every { fileOperations.exists(AFile("/path1/sub")) } returns true
        every { fileOperations.parent(AFile("/path1/sub")) } returns AFile("/path1")
        every { fileOperations.list(AFile("/path1/sub")) } returns listOf(
            AFile("/path1/sub/file1.mp3"),
            AFile("/path1/sub/file2.mkv")
        )
        every { fileOperations.properties(AFile("/path1/sub")) } returns AFileProperties(
            file = AFile("/path1/sub"),
            name = "sub",
            size = 1000,
            isDirectory = true
        )
        every { fileOperations.properties(AFile("/path1/sub/file1.mp3")) } returns AFileProperties(
            file = AFile("/path1/sub/file1.mp3"),
            name = "file1.mp3",
            size = 2000,
            isDirectory = false
        )
        every { fileOperations.properties(AFile("/path1/sub/file2.mkv")) } returns AFileProperties(
            file = AFile("/path1/sub/file2.mkv"),
            name = "file2.mkv",
            size = 3000,
            isDirectory = false
        )
        val actual: PlaylistAndChildrenDomain? = sut.getFolderList("path1/sub")
        log.d(actual?.playlist.toString())
        log.d(actual?.children.toString())
        assertNotNull(actual!!)
        assertEquals("sub", actual.playlist.title)
        assertEquals(FILESYSTEM, actual.playlist.platform)
        assertEquals("path1/sub", actual.playlist.platformId)
        assertEquals(1, actual.children.size) // has parent folder link
        assertEquals(2, actual.playlist.items.size)
        // verify parent folder link
        assertEquals(PARENT_FOLDER_TEXT, actual.children[0].title)
        assertEquals(FILESYSTEM, actual.children[0].platform)
        assertEquals("path1", actual.children[0].platformId)

        assertEquals("file1.mp3", actual.playlist.items[0].media.title)
        assertEquals(FILESYSTEM, actual.playlist.items[0].media.platform)
        assertEquals("path1/sub/file1.mp3", actual.playlist.items[0].media.platformId)

        assertEquals("file2.mkv", actual.playlist.items[1].media.title)
        assertEquals(FILESYSTEM, actual.playlist.items[1].media.platform)
        assertEquals("path1/sub/file2.mkv", actual.playlist.items[1].media.platformId)
    }

    @Test
    fun getFolderList_bad_path_throws_returns_null() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")

        val actual = sut.getFolderList("path4")
        assertNull(actual)
    }

    @Test
    fun getFolderList_bad_sub_path_returns_null() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")

        val actual: PlaylistAndChildrenDomain? = sut.getFolderList("path1/ddd")
        assertNull(actual)
    }

    @Test
    fun getFolderList_with_full_path_returns_null() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        every { fileOperations.exists(AFile("/path1")) } returns true
        every { fileOperations.properties(AFile("/path1")) } returns AFileProperties(
            file = AFile("/path1"),
            name = "path1",
            size = 0,
            isDirectory = true
        )
        val actual: PlaylistAndChildrenDomain? = sut.getFolderList("/path1")
        assertNull(actual)
    }

    @Test
    fun getFolderList_file_path_returns_null() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        every { fileOperations.exists(AFile("/path1")) } returns true
        every { fileOperations.exists(AFile("/path1/test.txt")) } returns true
        every { fileOperations.properties(AFile("/path1")) } returns AFileProperties(
            file = AFile("/path1"),
            name = "sub",
            size = 1000,
            isDirectory = true
        )
        every { fileOperations.properties(AFile("/path1/test.txt")) } returns AFileProperties(
            file = AFile("/path1/test.txt"),
            name = "test.txt",
            size = 1000,
            isDirectory = false
        )
        val actual: PlaylistAndChildrenDomain? = sut.getFolderList("path1/test.txt")
        assertNull(actual)
    }

    @Test
    fun `checkFolderPathIsInAllowedSet$Cuer_shared_commonMain`() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        assertTrue(sut.checkFolderPathIsInAllowedSet("/path1/sub/sub/test"))
        assertFalse(sut.checkFolderPathIsInAllowedSet("/c/path1/sub/sub/test"))
        assertTrue(sut.checkFolderPathIsInAllowedSet("/x/y/z/path2/sub/sub/test"))
        assertTrue(sut.checkFolderPathIsInAllowedSet("/x/y/z/path2/"))
    }

    @Test
    fun `fullToTruncatedFolderPath$Cuer_shared_commonMain`() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        assertEquals("path1/test1/test2", sut.fullToTruncatedFolderPath("/path1/test1/test2"))
        assertEquals(null, sut.fullToTruncatedFolderPath("/nopath/test1/test2"))
        assertEquals("path2/test1/test2", sut.fullToTruncatedFolderPath("/x/y/z/path2/test1/test2"))
    }

    @Test
    fun `truncatedToFullFolderPath$Cuer_shared_commonMain`() {
        every { prefs.folderRoots } returns setOf("/path1", "/x/y/z/path2", "/a/path3")
        assertEquals("/path1/test1/test2", sut.truncatedToFullFolderPath("path1/test1/test2"))
        assertEquals(null, sut.truncatedToFullFolderPath("/nopath/test1/test2"))
        assertEquals("/x/y/z/path2/test1/test2", sut.truncatedToFullFolderPath("path2/test1/test2"))
    }
}
