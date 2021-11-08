import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
}

val ver_coroutines_core: String by project
val ver_kotlinx_serialization_core: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_mockk: String by project
val ver_jvm: String by project
val ver_jfixture: String by project
val ver_junit: String by project
val ver_truth: String by project
val ver_mvikotlin: String by project
val ver_kotlinx_coroutines_test: String by project
val ver_multiplatform_settings: String by project
val ver_turbine: String by project
val ver_ktor: String by project
val ver_korio: String by project
val app_compileSdkVersion: String by project

version = "1.0"

android {
    compileSdkVersion(app_compileSdkVersion.toInt())
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(30)
    }
    // remove when upgrading to kotlin 1.5
    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}
// For JVM only

// For Android only

// For JS only

kotlin {
    jvm()
    js {
        browser()
    }
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
                implementation("io.insert-koin:koin-core:$ver_koin")
                implementation("com.arkivanov.mvikotlin:mvikotlin:$ver_mvikotlin")
                implementation("com.arkivanov.mvikotlin:mvikotlin-main:$ver_mvikotlin")
                implementation("com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$ver_mvikotlin")
                implementation("com.russhwolf:multiplatform-settings:$ver_multiplatform_settings")
                implementation("com.russhwolf:multiplatform-settings-no-arg:$ver_multiplatform_settings")
                implementation("io.ktor:ktor-client-core:$ver_ktor")
                implementation("com.soywiz.korlibs.korio:korio:$ver_korio")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.insert-koin:koin-test:$ver_koin")
                implementation("io.mockk:mockk:$ver_mockk")
            }
        }
//        val jvmAndroidSharedMain = create("jvmAndroidSharedMain") {
//            dependsOn(commonMain)
//            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime")
//            }
//        }

        val jvmMain by getting {
//            dependsOn(jvmAndroidSharedMain)
            kotlin.srcDir("src/jvmAndroidSharedMain/kotlin")
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio-jvm:$ver_korio")
            }
        }
        val jvmTest by getting {
            dependencies {
                // Koin for JUnit 4
                implementation("io.insert-koin:koin-test-junit4:$ver_koin")
                implementation("com.flextrade.jfixture:jfixture:$ver_jfixture")
                implementation("com.google.truth:truth:$ver_truth")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_kotlinx_coroutines_test")
                implementation("app.cash.turbine:turbine:$ver_turbine")
            }
        }
        val androidMain by getting {
            //dependsOn(jvmAndroidSharedMain)
            kotlin.srcDir("src/jvmAndroidSharedMain/kotlin")
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio-android:$ver_korio")
            }
        }
        val androidTest by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("com.soywiz.korlibs.korio:korio-js:$ver_korio")
            }
        }

        val jsTest by getting
    }
}

//https://stackoverflow.com/questions/55456176/unresolved-reference-compilekotlin-in-build-gradle-kts
tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = ver_jvm
    }
}
