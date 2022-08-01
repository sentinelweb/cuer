plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

version = "1.0"

val ver_sqldelight: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project

kotlin {
    android()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
//    js {
//        browser()
//    }
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        version = "1"
        framework {
            baseName = "database"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
//                implementation(project(":shared"))
                implementation("com.squareup.sqldelight:android-driver:$ver_sqldelight")
            }
        }
        val androidTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
//                implementation(project(":shared"))
                implementation("com.squareup.sqldelight:native-driver:$ver_sqldelight")
            }

        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
//        val jsMain by getting {
//            dependencies {
//                implementation("com.squareup.sqldelight:sqljs-driver:$ver_sqldelight")
//            }
//        }
//        val jsTest by getting
    }
}

android {
    compileSdk = 32
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}

sqldelight {
    database("Database") { // This will be the name of the generated database class.
        packageName = "uk.co.sentinelweb.cuer.app.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/commonMain/sqldelight/uk/co/sentinelweb/cuer/app/database")
        verifyMigrations = true
    }
}
