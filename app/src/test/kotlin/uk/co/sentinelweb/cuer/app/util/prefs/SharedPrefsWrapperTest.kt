package uk.co.sentinelweb.cuer.app.util.prefs

import android.app.Application
import android.content.SharedPreferences
import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.CURRENT_PLAYING_PLAYLIST
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.ext.build

class SharedPrefsWrapperTest {

    @MockK
    lateinit var mockPrefs: SharedPreferences

    @MockK
    lateinit var mockPrefsEditor: SharedPreferences.Editor

    @MockK
    lateinit var mockApp: Application

    private val fixture = JFixture()

//    enum class TestEnumType() { V1, V2, V3 }
//    enum class TestPreferencesType(override val fname: String) : Field { X1("x1"), X2("x2"), X3("x3") }

    private lateinit var sut: SharedPrefsWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = SharedPrefsWrapper(
            mockApp,
            SystemLogWrapper()
        )
        every { mockApp.getSharedPreferences(GeneralPreferences::class.simpleName, 0) } returns mockPrefs
        every { mockPrefs.edit() } returns mockPrefsEditor
        every { mockPrefsEditor.putString(any(), any()) } returns mockPrefsEditor
        every { mockPrefsEditor.putLong(any(), any()) } returns mockPrefsEditor
        every { mockPrefsEditor.putInt(any(), any()) } returns mockPrefsEditor
        every { mockPrefsEditor.putBoolean(any(), any()) } returns mockPrefsEditor
        every { mockPrefsEditor.remove(any()) } returns mockPrefsEditor
    }

    @Test
    fun getLong() {
        val fixtValue: Long = fixture.build()
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getLong(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun getLongDefault() {
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getLong(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtDefault)
        verify { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun getLongNullable() {
        val fixtValue: Long = fixture.build()
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns true
        every { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, 0) } returns fixtValue

        assertThat(sut.getLong(CURRENT_PLAYING_PLAYLIST)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, 0L) }
        verify { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) }
    }

    @Test
    fun getLongNullable_isNull() {
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns false
        every { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getLong(CURRENT_PLAYING_PLAYLIST)).isNull()
        verify { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) }
    }

    @Test
    fun putLong() {
        val fixtValue: Long = fixture.build()
        every { mockPrefsEditor.putLong(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) } returns mockPrefsEditor

        sut.putLong(CURRENT_PLAYING_PLAYLIST, fixtValue)

        verify { mockPrefsEditor.putLong(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) }
        verify { mockPrefsEditor.commit() }
    }

    @Test
    fun getInt() {
        val fixtValue: Long = fixture.build()
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getLong(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun getIntDefault() {
        val fixtDefault: Int = fixture.build()
        every { mockPrefs.getInt(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getInt(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtDefault)
        verify { mockPrefs.getInt(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun getIntNullable() {
        val fixtValue: Int = fixture.build()
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns true
        every { mockPrefs.getInt(CURRENT_PLAYING_PLAYLIST.fname, 0) } returns fixtValue

        assertThat(sut.getInt(CURRENT_PLAYING_PLAYLIST)).isEqualTo(fixtValue)
        verify { mockPrefs.getInt(CURRENT_PLAYING_PLAYLIST.fname, 0) }
        verify { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) }
    }

    @Test
    fun getIntNullable_isNull() {
        val fixtDefault: Int = fixture.build()
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns false
        every { mockPrefs.getInt(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getInt(CURRENT_PLAYING_PLAYLIST)).isNull()
        verify { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) }
    }

    @Test
    fun putInt() {
        val fixtValue: Int = fixture.build()
        every { mockPrefsEditor.putInt(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) } returns mockPrefsEditor

        sut.putInt(CURRENT_PLAYING_PLAYLIST, fixtValue)

        verify { mockPrefsEditor.putInt(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun getString() {
        val fixtValue: String = fixture.build()
        val fixtDefault: String = fixture.build()
        every { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getString(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun putString() {
        val fixtValue: String = fixture.build()
        every { mockPrefsEditor.putString(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) } returns mockPrefsEditor

        sut.putString(CURRENT_PLAYING_PLAYLIST, fixtValue)

        verify { mockPrefsEditor.putString(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun putEnum() {
        val fixtValue: GeneralPreferences = fixture.build()
        every {
            mockPrefsEditor.putString(
                CURRENT_PLAYING_PLAYLIST.fname,
                fixtValue.toString()
            )
        } returns mockPrefsEditor

        sut.putEnum(CURRENT_PLAYING_PLAYLIST, fixtValue)

        verify { mockPrefsEditor.putString(CURRENT_PLAYING_PLAYLIST.fname, fixtValue.toString()) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun getEnum() {
        val fixtValue: GeneralPreferences = fixture.build()
        val fixtDefault: GeneralPreferences = fixture.build()
        every { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, null) } returns fixtValue.toString()

        assertThat(sut.getEnum(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)

        verify { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, null) }
    }

    @Test
    fun getEnumDefault() {
        val fixtDefault: GeneralPreferences = fixture.build()
        every { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, null) } returns fixtDefault.toString()

        assertThat(sut.getEnum(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtDefault)

        verify { mockPrefs.getString(CURRENT_PLAYING_PLAYLIST.fname, null) }
    }

    @Test
    fun getBoolean() {
        val fixtValue: Boolean = fixture.build()
        val fixtDefault: Boolean = !fixtValue
        every { mockPrefs.getBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getBoolean(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun getBooleanDefault() {
        val fixtValue: Boolean = fixture.build()
        val fixtDefault: Boolean = !fixtValue
        every { mockPrefs.getBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getBoolean(CURRENT_PLAYING_PLAYLIST, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtDefault) }
    }

    @Test
    fun putBoolean() {
        val fixtValue: Boolean = fixture.build()
        every { mockPrefsEditor.putBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) } returns mockPrefsEditor

        sut.putBoolean(CURRENT_PLAYING_PLAYLIST, fixtValue)

        verify { mockPrefsEditor.putBoolean(CURRENT_PLAYING_PLAYLIST.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun remove() {
        every { mockPrefsEditor.remove(CURRENT_PLAYING_PLAYLIST.fname) } returns mockPrefsEditor
        every { mockPrefsEditor.remove(CP_FIRST) } returns mockPrefsEditor
        every { mockPrefsEditor.remove(CP_SECOND) } returns mockPrefsEditor
        sut.remove(CURRENT_PLAYING_PLAYLIST)
        verify { mockPrefsEditor.remove(CURRENT_PLAYING_PLAYLIST.fname) }
        verify { mockPrefsEditor.remove(CP_FIRST) }
        verify { mockPrefsEditor.remove(CP_SECOND) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun has() {
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns true
        every { mockPrefs.contains(CP_FIRST) } returns false

        assertThat(sut.has(CURRENT_PLAYING_PLAYLIST)).isTrue()
    }

    @Test
    fun hasPair() {
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns false
        every { mockPrefs.contains(CP_FIRST) } returns true

        assertThat(sut.has(CURRENT_PLAYING_PLAYLIST)).isTrue()
    }

    @Test
    fun hasNone() {
        every { mockPrefs.contains(CURRENT_PLAYING_PLAYLIST.fname) } returns false
        every { mockPrefs.contains(CP_FIRST) } returns false

        assertThat(sut.has(CURRENT_PLAYING_PLAYLIST)).isFalse()
    }

    @Test
    fun putPair() {
        sut.putPair(CURRENT_PLAYING_PLAYLIST, 1L to MEMORY)
        verify { mockPrefsEditor.putLong(CP_FIRST, 1L) }
        verify { mockPrefsEditor.putString(CP_SECOND, MEMORY.toString()) }
        verify(exactly = 2) { mockPrefsEditor.apply() }
    }

    @Test
    fun putPairNulls() {
        sut.putPair(CURRENT_PLAYING_PLAYLIST, (null as String?) to (null as Source?))
        verify { mockPrefsEditor.remove(CP_FIRST) }
        verify { mockPrefsEditor.remove(CP_SECOND) }
        verify(exactly = 2) { mockPrefsEditor.apply() }
    }

    @Test
    fun getPair() {
        val fixtFirst: String = fixture.build()
        val fixtFirstDefault: String = fixture.build()
        val fixtSecond: Source = fixture.build()
        val fixtSecondDefault: Source = fixture.build()
        every { mockPrefs.getString(CP_FIRST, fixtFirstDefault) } returns fixtFirst
        every { mockPrefs.getString(CP_SECOND, null) } returns fixtSecond.toString()

        val pair = sut.getPair(CURRENT_PLAYING_PLAYLIST, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirst)
        assertThat(pair.second).isEqualTo(fixtSecond)
    }

    @Test
    @Ignore("Some mocking problem there")
    fun getPairDefault() {
        val fixtFirstDefault: String = fixture.build()
        val fixtSecondDefault: Source = fixture.build()
        every { mockPrefs.getString(CP_FIRST, fixtFirstDefault) } returns null
        every { mockPrefs.getString(CP_SECOND, null) } returns null

        val pair = sut.getPair(CURRENT_PLAYING_PLAYLIST, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirstDefault)
        assertThat(pair.second).isEqualTo(fixtSecondDefault)
    }

    companion object {
        private val CP_FIRST = "currentPlaylist.first"
        private val CP_SECOND = "currentPlaylist.second"
    }
}