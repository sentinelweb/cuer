package uk.co.sentinelweb.cuer.app.util.firebase

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.domain.ImageDomain

class FirebaseDefaultImageProvider constructor(
    private val log: LogWrapper
) {
    private lateinit var rootStorageRef: StorageReference
    private lateinit var listStorageRef: List<StorageReference>
    private var isInitialising: Boolean = false

    init {
        log.tag(this)
    }

    private val isInitialised: Boolean
        get() = this::listStorageRef.isInitialized

    fun init() {
        isInitialising = true
        rootStorageRef = Firebase.storage.getReferenceFromUrl(ROOT)
        rootStorageRef.listAll().apply {
            addOnCompleteListener {
                isInitialising = false
            }
            addOnSuccessListener { listResult ->
                if (listResult.items.size > 0) {
                    listStorageRef = listResult.items

                }
            }
            addOnFailureListener { ex -> log.e("Couldn't init ") }
        }
    }

    fun checkToInit() {
        if (!isInitialised && !isInitialising) {
            init()
        }
    }

    fun getNextImage(
        current: ImageDomain,
        forward: Boolean = true,
        callback: (ImageDomain?) -> Unit
    ) {
        if (isInitialised) {
            listStorageRef
                .find { ref: StorageReference ->
                    current.url.endsWith(ref.path)
                }
                ?.let { listStorageRef.indexOf(it) }
                ?.let { cIndex -> if (forward) wrapInc(cIndex) else wrapDec(cIndex) }
                ?.let {
                    listStorageRef[it].apply {
                        callback(ImageDomain(url = "$root$path", width = null, height = null))
                    }
                }
                ?: callback(null)

        } else {
            checkToInit()
            callback(null)
        }
    }

    private fun wrapInc(cindex: Int): Int {
        var next = cindex + 1
        next %= listStorageRef.size
        return next
    }

    private fun wrapDec(cindex: Int): Int {
        var next = cindex - 1
        if (next < 0) next = listStorageRef.size - 1
        return next
    }

    fun makeRef(image: ImageDomain): StorageReference =
        Firebase.storage.getReferenceFromUrl(image.url)

    fun makeRef(url: String): StorageReference = Firebase.storage.getReferenceFromUrl(url)

    companion object {
        private val ROOT = "gs://cuer-275020.appspot.com/playlist_header/"
    }
}