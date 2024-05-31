package uk.co.sentinelweb.cuer.app.usecase

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.core.providers.TimeProvider
import uk.co.sentinelweb.cuer.domain.creator.GuidCreator
import kotlin.test.assertEquals

class GetFolderListUseCaseTest {

    private val prefs: MultiPlatformPreferencesWrapper = mockk(relaxed = true)
    private val fileOperations: PlatformFileOperation = mockk(relaxed = true)
    private val guidCreator: GuidCreator = mockk(relaxed = true)
    private val timeProvider: TimeProvider = mockk(relaxed = true)

    private lateinit var sut: GetFolderListUseCase

    @Before
    fun setUp() {
        sut = GetFolderListUseCase(prefs, fileOperations, guidCreator, timeProvider)
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