plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    //id("kotlin")
}

version = "1.0"

kotlin {
    js {
        //browser()// use in usage lib
    }
    android()

    sourceSets {
        val commonMain by getting
        val androidMain by getting
        val jsMain by getting
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
    }
}
