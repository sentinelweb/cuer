plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
//    kotlin("com.android.application")
    id("com.squareup.sqldelight")
    kotlin("plugin.serialization")
}

group = "uk.co.sentinelweb.cuer"
version = "1.0"

//val ver_jvm: String by project
val ver_coroutines: String by project
val ver_kotlinx_serialization_core: String by project
val ver_sqldelight: String by project
val ver_kotlinx_datetime: String by project
val ver_koin: String by project
val ver_turbine: String by project
val ver_kotlin_fixture: String by project
val ver_mockk: String by project

val ver_swift_tools: String by project
val ver_ios_deploy_target: String by project

kotlin {
    jvm()
    androidTarget {
        compilations.all {
            kotlinOptions.jvmTarget = libs.versions.jvm.get()
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js(IR) {
        browser()
    }
    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = ver_ios_deploy_target
        version = "1"
        framework {
            baseName = "database"
            isStatic = true //or false
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(libs.kotlinxDatetime)
                implementation(libs.kotlinxCoroutinesCore)
                implementation(libs.kotlinxSerializationCore)
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.koinCore)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(kotlin("test"))
                implementation(libs.koinTest)
                implementation(libs.kotlinxCoroutinesTest)
                implementation(libs.turbine)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.sqldelightAndroidDriver)
                implementation(libs.koinAndroid)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(project(":domain"))
                implementation(libs.koinTestJUnit4)
                implementation(libs.sqldelightSqliteDriver)
                implementation(libs.kotlinFixture)
                implementation(libs.mockkAndroid)
                implementation(libs.mockkAgent)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.sqldelightNativeDriver)
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
        val jsMain by getting {
            dependencies {
                implementation(libs.sqldelightSqlJsDriver)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.sqldelightSqliteDriver)
            }
        }
        val jsTest by getting
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

// generate schema ./gradlew generateCommonMainDatabaseSchema
// verify schema ./gradlew verifySqlDelightMigration
sqldelight {
    database("Database") { // This will be the name of the generated database class.
        packageName = "uk.co.sentinelweb.cuer.app.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/commonMain/sqldelight/uk/co/sentinelweb/cuer/database/verify")
        verifyMigrations = true
//        deriveSchemaFromMigrations = true
    }
}
