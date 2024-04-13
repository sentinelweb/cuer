import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress

class PlatformWifiInfo {

    @Throws(IOException::class)
    fun getEssid(): String {
        val osName = System.getProperty("os.name").toLowerCase()
        return when {
            osName.contains("linux") -> getEssidLinux()
            osName.contains("windows") -> getEssidWindows()
            osName.contains("mac") -> getEssidMac()
            else -> "Unsupported OS"
        }
    }

    @Throws(IOException::class)
    fun getIpAddress(): String {
        val osName = System.getProperty("os.name").toLowerCase()
        return when {
            osName.contains("linux") -> getIpAddressLinux()
            osName.contains("windows") -> getIpAddressWindows()
            osName.contains("mac") -> getIpAddressMac()
            else -> "Unsupported OS"
        }
    }

    @Throws(IOException::class)
    private fun getEssidLinux(): String {
        val process = Runtime.getRuntime().exec("iwgetid -r")
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            return reader.readLine().trim()
        }
    }

    @Throws(IOException::class)
    private fun getEssidWindows(): String {
        val process = Runtime.getRuntime().exec("netsh wlan show interfaces")
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.trim { it <= ' ' }.startsWith("SSID")) {
                    return line!!.split(":")[1].trim { it <= ' ' }
                }
            }
        }
        return "Unknown"
    }

    @Throws(IOException::class)
    private fun getEssidMac(): String {
        val path = "/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources"
        val process = Runtime.getRuntime().exec("${path}/airport -I")

        // Read the output of the command
        val output = process.inputStream.bufferedReader().use(BufferedReader::readText)

        // Extract the ESSID from the output using a regular expression
        val essidPattern = Regex("""\bSSID: (.+)\b""")
        val matchResult = essidPattern.find(output)

        // Return the ESSID if found, otherwise return "Unknown"
        return matchResult?.groupValues?.get(1) ?: "Unknown"
    }


    @Throws(IOException::class)
    private fun getIpAddressLinux(): String {
        val process = Runtime.getRuntime().exec("hostname -I")
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            return reader.readLine().trim()
        }
    }

    @Throws(IOException::class)
    private fun getIpAddressWindows(): String {
        return InetAddress.getLocalHost().hostAddress
    }

    @Throws(IOException::class)
    private fun getIpAddressMac(): String {
        // getIpAddressMacPrint()
        val cmd = arrayOf("/bin/sh", "-c", "ifconfig en0 | awk '/inet / {print $2}'")
        val process = Runtime.getRuntime().exec(cmd)
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            return reader.readLine().toString()
        }
    }

    private fun getIpAddressMacPrint(): String {
        val cmd = arrayOf("/bin/sh", "-c", "ifconfig en0 | awk '/inet / {print $2}'")
        val process = Runtime.getRuntime().exec(cmd)
        var output = ""
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            output = reader.readLines().toString()
        }
        BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
            val error = reader.readLines()
            if (error.isNotEmpty()) {
                output += " Error: $error"
            }
        }
        println(output)
        return output
    }

    fun essTwst() {
        try {
            // Run the command
            val process = Runtime.getRuntime()
                .exec("/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport -I | awk '/ SSID/{print substr($0, index($0, $2))}'")

            // Capture standard output
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while ((outputReader.readLine().also { line = it }) != null) {
                output.append(line).append('\n')
            }

            // Capture standard error
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val error = StringBuilder()
            while ((errorReader.readLine().also { line = it }) != null) {
                error.append(line).append('\n')
            }

            // Wait for the process to complete
            val exitCode = process.waitFor()

            // Print the output
            println("Output:")
            println(output.toString())

            // Print the error, if any
            if (exitCode != 0) {
                System.err.println("Error:")
                System.err.println(error.toString())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}
