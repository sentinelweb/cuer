package uk.co.sentinelweb.cuer.hub.util.remote

import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.Cipher

class FileEncryption(
    private val keyStoreManager: KeyStoreManager
) {
    @Throws(Exception::class)
    fun encrypt(inputData: String, outputFile: String?) {
        val skeySpec = keyStoreManager.getKey()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)

        val inputBytes = inputData.toByteArray()
        val outputBytes = cipher.doFinal(inputBytes)

        Files.write(Paths.get(outputFile), outputBytes)
    }

    @Throws(Exception::class)
    fun decrypt(inputFile: String): String {
        val skeySpec = keyStoreManager.getKey()
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)

        val inputBytes = Files.readAllBytes(Paths.get(inputFile))
        val outputBytes = cipher.doFinal(inputBytes)

        return String(outputBytes)
    }

    companion object {
        private const val ALGORITHM = "AES"
    }
}