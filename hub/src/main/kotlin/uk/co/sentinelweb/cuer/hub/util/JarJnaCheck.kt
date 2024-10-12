package uk.co.sentinelweb.cuer.hub.util

import java.io.File

class JarJnaCheck {
    fun check() {
        // Get system class loader
        // Get system classpath from system property
        val classpath = System.getProperty("java.class.path")

        // Split the classpath into individual paths
        val classpathEntries = classpath.split(File.pathSeparator)

    // Define a map to store JNA jar files and their versions
        val jarMap = mutableMapOf<String, String>()

        // Specify the JAR name pattern to check for (case insensitive)
        val jarToCheck = "jna"

        // Iterate through classpath URLs
        for (entry in classpathEntries) {
            val file = File(entry)

            if (file.name.lowercase().contains(jarToCheck.lowercase())) {
                val jarName = file.name
                val jarVersion = getJarVersion(jarName)

                if (jarMap.containsKey(jarName)) {
                    println("Multiple versions found for $jarName:")
                    println("Existing Version: ${jarMap[jarName]}")
                    println("New Version: $jarVersion")
                } else {
                    jarMap[jarName] = jarVersion
                }
            }
        }

        // Print all JNA jars found
        if (jarMap.isEmpty()) {
            println("No JNA jars found.")
        } else {
            println("JNA jars found:")
            for ((jarName, jarVersion) in jarMap) {
                println("JAR: $jarName, Version: $jarVersion")
            }
        }
    }

    fun getJarVersion(jarName: String): String {
        // Simple method to extract version from a jar file name
        // This assumes jar name format: name-version.jar
        // e.g., jna-5.10.0.jar will return 5.10.0
        val dashIndex = jarName.lastIndexOf('-')
        val dotIndex = jarName.lastIndexOf('.')

        return if (dashIndex != -1 && dotIndex != -1 && dashIndex < dotIndex) {
            jarName.substring(dashIndex + 1, dotIndex)
        } else {
            "Unknown Version"
        }
    }
}
