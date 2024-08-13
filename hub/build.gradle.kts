import org.jetbrains.compose.desktop.application.dsl.TargetFormat

// ./gradlew packageDmg

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

val isProduction: String by project
val CUER_BG_PLAY: String by project
val CUER_REMOTE_ENABLED: String by project
val CUER_HUB_STORE_KEY: String by project
val CUER_HUB_STORE_PASS: String by project

group = "uk.co.sentinlweb.cuer"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(project(":database"))
    implementation(project(":shared"))
    implementation(project(":domain"))
    implementation(project(":remote"))
    implementation(project(":net"))
    implementation(compose.desktop.currentOs)

    implementation(libs.koinCore)
    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxCoroutinesJdk8)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientCio)
    implementation(libs.batikTranscoder)
    implementation(libs.multiplatformSettings)
    implementation(libs.vlcj)
    implementation(libs.jna)
    implementation(libs.jnaPlatform)

    testImplementation(libs.junit)
    testImplementation(libs.koinTest)
    testImplementation(libs.koinTestJUnit4)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinxCoroutinesTest)
}
// lots on configuration info here
// https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md
compose.desktop {
    application {
        mainClass = "uk.co.sentinelweb.cuer.hub.main.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Cuer Hub"
            packageVersion = "1.0.0"// fixme should use app_versionName but there are a lot of platform dependent rules
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = libs.versions.jvm.get()
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = libs.versions.jvm.get()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.create<JavaExec>("runTestMain") {
    mainClass = "uk.co.sentinelweb.cuer.hub.main.TestMainKt"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register("generateApiKeyClass") {
    doLast {
        val file = File("$projectDir/src/main/kotlin/uk/co/sentinelweb/cuer/net/key/ApiKey.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package uk.co.sentinelweb.cuer.net.key
            
            import uk.co.sentinelweb.cuer.net.ApiKeyProvider
            
            class CuerPixabayApiKeyProvider : ApiKeyProvider {
                override val key: String = "${project.findProperty("CUER_PIXABAY_API_KEY") ?: ""}"
            }
            class CuerYoutubeApiKeyProvider : ApiKeyProvider {
                override val key: String = "${project.findProperty("CUER_YOUTUBE_API_KEY") ?: ""}"
            }
        """.trimIndent()
        )
    }
}

tasks.register("generateBuildConfigInjectClass") {
    doLast {
        val file = File("$projectDir/src/main/kotlin/uk/co/sentinelweb/cuer/hub/BuildConfigInject.kt")
        file.parentFile.mkdirs()
        file.writeText(
            """
            package uk.co.sentinelweb.cuer.hub
            
            object BuildConfigInject {
                val isDebug: Boolean = ${isProduction}.not()
                val cuerRemoteEnabled: Boolean = $CUER_REMOTE_ENABLED
                val cuerBgPlayEnabled: Boolean = $CUER_BG_PLAY
                val versionCode: Int = ${libs.versions.app.versionCode.get().toInt()}
                val version: String = "${libs.versions.app.versionName.get()}"
                val hubStoreKey: String = "$CUER_HUB_STORE_KEY"
                val hubStorePass: String = "$CUER_HUB_STORE_PASS"
            }
        """.trimIndent()
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateApiKeyClass")
    dependsOn("generateBuildConfigInjectClass")
}
