package uk.co.sentinelweb.cuer.app.util.prefs

import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flextrade.jfixture.JFixture
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.co.sentinelweb.cuer.app.CuerTestApp
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source
import uk.co.sentinelweb.cuer.app.orchestrator.OrchestratorContract.Source.MEMORY
import uk.co.sentinelweb.cuer.app.util.prefs.GeneralPreferences.TEST_ID
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.tools.ext.build

@RunWith(AndroidJUnit4::class)
@Config(application = CuerTestApp::class)
class SharedPrefsWrapperRoboTest {

    private lateinit var prefs: SharedPreferences


    private val fixture = JFixture()

//    enum class TestPreferencesType(override val fname: String) : Field { X1("x1"), X2("x2"), X3("x3") }

    private lateinit var sut: SharedPrefsWrapper

    @Before
    fun setUp() {

        sut = SharedPrefsWrapper(
            ApplicationProvider.getApplicationContext(),
            SystemLogWrapper()
        )
        prefs = ApplicationProvider.getApplicationContext<CuerTestApp>()
            .getSharedPreferences(GeneralPreferences::class.simpleName, 0)
    }

    @Test
    fun getLong() {
    }

    @Test
    fun testGetLong() {
    }

    @Test
    fun putLong() {
    }

    @Test
    fun getInt() {
    }

    @Test
    fun testGetInt() {
    }

    @Test
    fun putInt() {
    }

    @Test
    fun getString() {
    }

    @Test
    fun putString() {
    }

    @Test
    fun putEnum() {
    }

    @Test
    fun getEnum() {
    }

    @Test
    fun getBoolean() {
    }

    @Test
    fun putBoolean() {
    }

    @Test
    fun remove() {
    }

    @Test
    fun has() {
    }

    @Test
    fun putPair() {
        sut.putPair(TEST_ID, 1L to MEMORY)

        assertThat(prefs.getLong(X1_FIRST, 0L)).isEqualTo(1L)
        assertThat(prefs.getString(X1_SECOND, null)).isEqualTo(MEMORY.toString())
    }

    @Test
    fun putPairNulls() {
        val fixtFirst: Long = fixture.build()
        val fixtSecond: Source = fixture.build()
        prefs.edit().putLong(X1_FIRST, fixtFirst).commit()
        prefs.edit().putString(X1_SECOND, fixtSecond.toString()).commit()

        sut.putPair(TEST_ID, (null as String?) to (null as Source?))

        assertThat(prefs.contains(X1_FIRST)).isFalse()
        assertThat(prefs.contains(X1_SECOND)).isFalse()
    }

    @Test
    fun getPair() {
        val fixtFirst: String = fixture.build()
        val fixtFirstDefault: String = fixture.build()
        val fixtSecond: Source = fixture.build()
        val fixtSecondDefault: Source = fixture.build()
        prefs.edit().putString(X1_FIRST, fixtFirst).commit()
        prefs.edit().putString(X1_SECOND, fixtSecond.toString()).commit()

        val pair = sut.getPair(TEST_ID, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirst)
        assertThat(pair.second).isEqualTo(fixtSecond)
    }

    @Test
    fun getPairDefault() {
        val fixtFirstDefault: String = fixture.build()
        val fixtSecondDefault: Source = fixture.build()

        val pair = sut.getPair(TEST_ID, fixtFirstDefault to fixtSecondDefault)

        assertThat(pair.first).isEqualTo(fixtFirstDefault)
        assertThat(pair.second).isEqualTo(fixtSecondDefault)
    }

    companion object {
        private val X1_FIRST = "testId.first"
        private val X1_SECOND = "testId.second"
    }
}
