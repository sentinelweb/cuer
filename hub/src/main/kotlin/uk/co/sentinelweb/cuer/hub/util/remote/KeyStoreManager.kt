package uk.co.sentinelweb.cuer.hub.util.remote

import uk.co.sentinelweb.cuer.app.db.repository.file.ConfigDirectory
import uk.co.sentinelweb.cuer.hub.BuildConfigInject.hubStoreKey
import uk.co.sentinelweb.cuer.hub.BuildConfigInject.hubStorePass
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class KeyStoreManager {

    fun generateKeysIfNecessary() {
        if (KeyStoreFile.exists().not()) {
            // Create a new KeyStore
            val keyStore = KeyStore.getInstance("JCEKS")
            keyStore.load(null, null)

            // Generate a new SecretKey
            val keyGen = KeyGenerator.getInstance("AES")
            val secureRandom = SecureRandom()
            keyGen.init(256, secureRandom)
            val secretKey = keyGen.generateKey()

            // Store the SecretKey
            val secretKeyEntry = KeyStore.SecretKeyEntry(secretKey)
            keyStore.setEntry(hubStoreKey, secretKeyEntry, KeyStore.PasswordProtection(hubStorePass.toCharArray()))

            // Save the keystore to a file
            val fos = FileOutputStream(KeyStoreFile.absolutePath)
            keyStore.store(fos, hubStorePass.toCharArray())
            fos.close()
        }
    }

    fun getKey(): SecretKey {
        // Now we can load the keystore again and retrieve the key
        val keyStore2 = KeyStore.getInstance("JCEKS")
        val fis = FileInputStream(KeyStoreFile.absolutePath)
        keyStore2.load(fis, hubStorePass.toCharArray())
        fis.close()

        val secretKeyEntry2 =
            keyStore2.getEntry(hubStoreKey, KeyStore.PasswordProtection(hubStorePass.toCharArray())) as KeyStore.SecretKeyEntry
        val myKey: SecretKey = secretKeyEntry2.secretKey
        return myKey
    }

    companion object {
        private val keystoreFileName = "cuer_keystore.jks"
        val KeyStoreFile: File = File(ConfigDirectory.Path, keystoreFileName)
    }

}