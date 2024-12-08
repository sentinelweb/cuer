import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.PointerType
import com.sun.jna.ptr.PointerByReference
import uk.co.sentinelweb.cuer.hub.util.sleep.SleepPreventer

// uses JNA to prevent sleep
class SleepPreventerMac : SleepPreventer {
    private var assertionIDKeeper: PointerByReference? = null
    private val iokit: IOKit by lazy { Native.load("IOKit", IOKit::class.java) }

    interface IOKit : Library {
        fun IOPMAssertionCreateWithName(
            assertionType: Pointer,
            assertionLevel: Int,
            assertionName: Pointer,
            assertionID: PointerByReference
        ): Int

        fun IOPMAssertionRelease(assertionID: Pointer): Int
    }

    interface CoreFoundation : Library {
        fun CFStringCreateWithCString(
            alloc: Pointer?,
            cStr: String,
            encoding: Int
        ): Pointer
    }

    class CFStringRef(s: String) : PointerType() {
        init {
            pointer = CoreFoundationLib.CFStringCreateWithCString(null, s, kCFStringEncodingUTF8)
        }
    }

    override fun preventSleep() {
        if (assertionIDKeeper == null) {
            val assertionID = PointerByReference()
            val assertionType = CFStringRef(ASSERTION_TYPE_NO_DISPLAY_SLEEP).pointer
            val assertionName = CFStringRef("Prevent sleep while playing video").pointer
            val iokit: IOKit = Native.load("IOKit", IOKit::class.java)

            val result =
                iokit.IOPMAssertionCreateWithName(assertionType, ASSERTION_LEVEL_ON, assertionName, assertionID)
            if (result != 0) {
                assertionIDKeeper = null
                println("Failed to create assertion: $result")
            } else {
                assertionIDKeeper = assertionID
                println("Sleep prevention activated.")
            }
        }
    }

    override fun allowSleep() {
        assertionIDKeeper?.let {
            val result = iokit.IOPMAssertionRelease(it.value)
            if (result == 0) {
                println("Sleep prevention deactivated.")
                assertionIDKeeper = null
            } else {
                println("Failed to release assertion: $result")
            }
        }
    }

    companion object {
        private val CoreFoundationLib: CoreFoundation by lazy {
            Native.load(
                "CoreFoundation",
                CoreFoundation::class.java
            )
        }
        private const val kCFStringEncodingUTF8 = 0x08000100
        private const val ASSERTION_TYPE_NO_DISPLAY_SLEEP = "NoDisplaySleepAssertion"
        private const val ASSERTION_LEVEL_ON = 255
    }

}
