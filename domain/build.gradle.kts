// build swift: ./gradlew shared:updatePackageSwift
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    kotlin("native.cocoapods")
}

group = "uk.co.sentinelweb.cuer"
version = "1.0"

kotlin {
    jvm()
    js(IR) {
        browser()
    }
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.get()
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        framework {
            isStatic = true //or false
        }
        summary = "domain"
        homepage = "https://sentinelweb.co.uk"
        ios.deploymentTarget = libs.versions.ios.deploy.target.get()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.kotlinxSerializationCore)
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlinxDatetime)
                implementation(libs.koinCore)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.koinTest)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.turbine)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.truth)
                implementation(libs.koinTestJUnit4)
                implementation(libs.kotlinFixture)
                implementation(libs.mockkAndroid)
                implementation(libs.mockkAgent)
                implementation(libs.kotlinxCoroutinesTest)
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
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
    }
}

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = libs.versions.app.base.get()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}