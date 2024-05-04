package uk.co.sentinelweb.cuer.tools.rule

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class FlakyTestRule(private val retryCount: Int) : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                // Implement retry logic here
                for (i in 0 until retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        System.err.println(description.displayName + ": run " + (i + 1) + " failed")
                    }
                }
                System.err.println(description.displayName + ": giving up after " + retryCount + " failures")
                throw caughtThrowable!!
            }
        }
    }
}