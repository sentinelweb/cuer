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
import uk.co.sentinelweb.cuer.app.util.prefs.SharedPrefsWrapperTest.TestPreferencesType.X1
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

    enum class TestEnumType() { V1, V2, V3 }
    enum class TestPreferencesType(override val fname: String) : Field { X1("x1"), X2("x2"), X3("x3") }

    private lateinit var sut: SharedPrefsWrapper<TestPreferencesType>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sut = SharedPrefsWrapper(
            TestPreferencesType::class,
            mockApp,
            SystemLogWrapper()
        )
        every { mockApp.getSharedPreferences(TestPreferencesType::class.simpleName, 0) } returns mockPrefs
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
        every { mockPrefs.getLong(X1.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getLong(X1, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(X1.fname, fixtDefault) }
    }

    @Test
    fun getLongDefault() {
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.getLong(X1.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getLong(X1, fixtDefault)).isEqualTo(fixtDefault)
        verify { mockPrefs.getLong(X1.fname, fixtDefault) }
    }

    @Test
    fun getLongNullable() {
        val fixtValue: Long = fixture.build()
        every { mockPrefs.contains(X1.fname) } returns true
        every { mockPrefs.getLong(X1.fname, 0) } returns fixtValue

        assertThat(sut.getLong(X1)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(X1.fname, 0L) }
        verify { mockPrefs.contains(X1.fname) }
    }

    @Test
    fun getLongNullable_isNull() {
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.contains(X1.fname) } returns false
        every { mockPrefs.getLong(X1.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getLong(X1)).isNull()
        verify { mockPrefs.contains(X1.fname) }
    }

    @Test
    fun putLong() {
        val fixtValue: Long = fixture.build()
        every { mockPrefsEditor.putLong(X1.fname, fixtValue) } returns mockPrefsEditor

        sut.putLong(X1, fixtValue)

        verify { mockPrefsEditor.putLong(X1.fname, fixtValue) }
        verify { mockPrefsEditor.commit() }
    }

    @Test
    fun getInt() {
        val fixtValue: Long = fixture.build()
        val fixtDefault: Long = fixture.build()
        every { mockPrefs.getLong(X1.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getLong(X1, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getLong(X1.fname, fixtDefault) }
    }

    @Test
    fun getIntDefault() {
        val fixtDefault: Int = fixture.build()
        every { mockPrefs.getInt(X1.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getInt(X1, fixtDefault)).isEqualTo(fixtDefault)
        verify { mockPrefs.getInt(X1.fname, fixtDefault) }
    }

    @Test
    fun getIntNullable() {
        val fixtValue: Int = fixture.build()
        every { mockPrefs.contains(X1.fname) } returns true
        every { mockPrefs.getInt(X1.fname, 0) } returns fixtValue

        assertThat(sut.getInt(X1)).isEqualTo(fixtValue)
        verify { mockPrefs.getInt(X1.fname, 0) }
        verify { mockPrefs.contains(X1.fname) }
    }

    @Test
    fun getIntNullable_isNull() {
        val fixtDefault: Int = fixture.build()
        every { mockPrefs.contains(X1.fname) } returns false
        every { mockPrefs.getInt(X1.fname, fixtDefault) } returns fixtDefault

        assertThat(sut.getInt(X1)).isNull()
        verify { mockPrefs.contains(X1.fname) }
    }

    @Test
    fun putInt() {
        val fixtValue: Int = fixture.build()
        every { mockPrefsEditor.putInt(X1.fname, fixtValue) } returns mockPrefsEditor

        sut.putInt(X1, fixtValue)

        verify { mockPrefsEditor.putInt(X1.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun getString() {
        val fixtValue: String = fixture.build()
        val fixtDefault: String = fixture.build()
        every { mockPrefs.getString(X1.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getString(X1, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getString(X1.fname, fixtDefault) }
    }

    @Test
    fun putString() {
        val fixtValue: String = fixture.build()
        every { mockPrefsEditor.putString(X1.fname, fixtValue) } returns mockPrefsEditor

        sut.putString(X1, fixtValue)

        verify { mockPrefsEditor.putString(X1.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun putEnum() {
        val fixtValue: TestEnumType = fixture.build()
        every { mockPrefsEditor.putString(X1.fname, fixtValue.toString()) } returns mockPrefsEditor

        sut.putEnum(X1, fixtValue)

        verify { mockPrefsEditor.putString(X1.fname, fixtValue.toString()) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun getEnum() {
        val fixtValue: TestEnumType = fixture.build()
        val fixtDefault: TestEnumType = fixture.build()
        every { mockPrefs.getString(X1.fname, null) } returns fixtValue.toString()

        assertThat(sut.getEnum(X1, fixtDefault)).isEqualTo(fixtValue)

        verify { mockPrefs.getString(X1.fname, null) }
    }

    @Test
    fun getEnumDefault() {
        val fixtDefault: TestEnumType = fixture.build()
        every { mockPrefs.getString(X1.fname, null) } returns fixtDefault.toString()

        assertThat(sut.getEnum(X1, fixtDefault)).isEqualTo(fixtDefault)

        verify { mockPrefs.getString(X1.fname, null) }
    }

    @Test
    fun getBoolean() {
        val fixtValue: Boolean = fixture.build()
        val fixtDefault: Boolean = !fixtValue
        every { mockPrefs.getBoolean(X1.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getBoolean(X1, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getBoolean(X1.fname, fixtDefault) }
    }

    @Test
    fun getBooleanDefault() {
        val fixtValue: Boolean = fixture.build()
        val fixtDefault: Boolean = !fixtValue
        every { mockPrefs.getBoolean(X1.fname, fixtDefault) } returns fixtValue

        assertThat(sut.getBoolean(X1, fixtDefault)).isEqualTo(fixtValue)
        verify { mockPrefs.getBoolean(X1.fname, fixtDefault) }
    }

    @Test
    fun putBoolean() {
        val fixtValue: Boolean = fixture.build()
        every { mockPrefsEditor.putBoolean(X1.fname, fixtValue) } returns mockPrefsEditor

        sut.putBoolean(X1, fixtValue)

        verify { mockPrefsEditor.putBoolean(X1.fname, fixtValue) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun remove() {
        every { mockPrefsEditor.remove(X1.fname) } returns mockPrefsEditor
        every { mockPrefsEditor.remove(X1_FIRST) } returns mockPrefsEditor
        every { mockPrefsEditor.remove(X1_SECOND) } returns mockPrefsEditor
        sut.remove(X1)
        verify { mockPrefsEditor.remove(X1.fname) }
        verify { mockPrefsEditor.remove(X1_FIRST) }
        verify { mockPrefsEditor.remove(X1_SECOND) }
        verify { mockPrefsEditor.apply() }
    }

    @Test
    fun has() {
        every { mockPrefs.contains(X1.fname) } returns true
        every { mockPrefs.contains(X1_FIRST) } returns false

        assertThat(sut.has(X1)).isTrue()
    }

    @Test
    fun hasPair() {
        every { mockPrefs.contains(X1.fname) } returns false
        every { mockPrefs.contains(X1_FIRST) } returns true

        assertThat(sut.has(X1)).isTrue()
    }

    @Test
    fun hasNone() {
        every { mockPrefs.contains(X1.fname) } returns false
        every { mockPrefs.contains(X1_FIRST) } returns false

        assertThat(sut.has(X1)).isFalse()
    }

    @Test
    fun putPair() {
        sut.putPair(X1, 1L to MEMORY)
        verify { mockPrefsEditor.putLong(X1_FIRST, 1L) }
        verify { mockPrefsEditor.putString(X1_SECOND, MEMORY.toString()) }
        verify(exactly = 2) { mockPrefsEditor.apply() }
    }

    @Test
    fun putPairNulls() {
        sut.putPair(X1, (null as String?) to (null as Source?))
        verify { mockPrefsEditor.remove(X1_FIRST) }
        verify { mockPrefsEditor.remove(X1_SECOND) }
        verify(exactly = 2) { mockPrefsEditor.apply() }
    }

    @Test
    fun getPair() {
        val fixtFirst: String = fixture.build()
        val fixtFirstDefault: String = fixture.build()
        val fixtSecond: Source = fixture.build()
        val fixtSecondDefault: Source = fixture.build()
        every { mockPrefs.getString(X1_FIRST, fixtFirstDefault) } returns fixtFirst
        every { mockPrefs.getString(X1_SECOND, null) } returns fixtSecond.toString()

        val pair = sut.getPair(X1, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirst)
        assertThat(pair.second).isEqualTo(fixtSecond)
    }

    @Test
    @Ignore("Some mocking problem there")
    fun getPairDefault() {
        val fixtFirstDefault: String = fixture.build()
        val fixtSecondDefault: Source = fixture.build()
        every { mockPrefs.getString(X1_FIRST, fixtFirstDefault) } returns null
        every { mockPrefs.getString(X1_SECOND, null) } returns null

        val pair = sut.getPair(X1, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirstDefault)
        assertThat(pair.second).isEqualTo(fixtSecondDefault)
    }

    companion object {
        private val X1_FIRST = "x1.first"
        private val X1_SECOND = "x1.second"
    }
}