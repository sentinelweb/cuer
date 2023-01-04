package uk.co.sentinelweb.cuer.app.util.link

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uk.co.sentinelweb.cuer.domain.TimecodeDomain

class TimecodeExtractorTest {
    private val sut  = TimecodeExtractor()

    @Test
    fun reduceTimecode() {
        assertEquals(123000, "02:03".timecodeToMillis())
        assertEquals(7203000, "2:00:03".timecodeToMillis())
    }

    @Test
    fun testTimecodeRegex() {
        assertEquals(TimecodeDomain(3723000, "1:2:3", 0 to 4), sut.extractTimecodes("1:2:3").firstOrNull())
        assertEquals(TimecodeDomain(123000, "2:3", 0 to 2), sut.extractTimecodes("2:3").firstOrNull())
        assertNull( sut.extractTimecodes("3").firstOrNull())
        assertNull( sut.extractTimecodes(":3").firstOrNull())
        assertEquals(TimecodeDomain(123000, "2:3" , 3 to 5), sut.extractTimecodes("f1:2:3").firstOrNull())
        assertEquals(TimecodeDomain(3723000, "01:02:03", 0 to 7), sut.extractTimecodes("01:02:03").firstOrNull())
        assertEquals(TimecodeDomain(123000, "02:03", 0 to 4), sut.extractTimecodes("02:03").firstOrNull())
        assertEquals(TimecodeDomain(123000, "02:03", 1 to 5), sut.extractTimecodes(":02:03").firstOrNull())
        assertEquals(TimecodeDomain(129723000, "36:02:03", 0 to 7), sut.extractTimecodes("36:02:03").firstOrNull())
        assertEquals(TimecodeDomain(0, "0:00", 0 to 3), sut.extractTimecodes("0:00").firstOrNull())
    }
}