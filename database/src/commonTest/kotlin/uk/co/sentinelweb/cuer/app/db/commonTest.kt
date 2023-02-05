package uk.co.sentinelweb.cuer.app.db

import uk.co.sentinelweb.cuer.db.Greeting
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class CommonGreetingTest {

    @Test
    @Ignore
    fun testExample() {
        assertTrue(Greeting().greeting().contains("Hello"), "Check 'Hello' is mentioned")
    }
}