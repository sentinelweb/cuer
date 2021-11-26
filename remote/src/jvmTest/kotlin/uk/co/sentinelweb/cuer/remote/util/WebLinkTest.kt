package uk.co.sentinelweb.cuer.remote.util

import org.junit.Test
import kotlin.test.assertEquals

class WebLinkTest {

    private val sut = WebLink()

    @Test
    fun basicLink() {
        val text = """
            dddd http://www.gmail.com dddd
        """.trimIndent()

        val textExpect = """
            dddd <a href="http://www.gmail.com" target="_blank">http://www.gmail.com</a> dddd
        """.trimIndent()

        assertEquals(textExpect, sut.replaceLinks(text))
    }

    @Test
    fun complexLinks() {
        val text = """
            dddd http://www.gmail.com dddd
            dddd www.gmail.com dddd
            dddd goo.gl/1 dddd
            dddd ggg. dddd
        """.trimIndent()

        val textExpect = """
            dddd <a href="http://www.gmail.com" target="_blank">http://www.gmail.com</a> dddd
            dddd <a href="www.gmail.com" target="_blank">www.gmail.com</a> dddd
            dddd <a href="goo.gl/1" target="_blank">goo.gl/1</a> dddd
            dddd ggg. dddd
        """.trimIndent()

        assertEquals(textExpect, sut.replaceLinks(text))
    }
}