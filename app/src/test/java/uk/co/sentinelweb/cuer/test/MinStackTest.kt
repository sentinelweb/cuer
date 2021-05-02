package uk.co.sentinelweb.cuer.test

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MinStackTest {

    @Before
    fun setUp() {
    }

    @Test
    fun test() {
        val minStack = MinStack<Int>()
        minStack.push(-2)
        minStack.push(0)
        minStack.push(-3)
        assertEquals(-3, minStack.min)       // --> Returns -3.
        minStack.pop()
        assertEquals(0, minStack.top)        // --> Returns 0.
        assertEquals(-2, minStack.min)       // --> Returns -2.
    }

}