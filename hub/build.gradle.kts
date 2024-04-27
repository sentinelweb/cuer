import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

val ver_jvm: String by project
val ver_koin: String by project
val ver_coroutines: String by project
val ver_ktor: String by project
val ver_batik: String by project
val ver_junit: String by project
val ver_mockk: String by project
val ver_turbine: String by project
val ver_multiplatform_settings: String by project

val isProduction: String by project
val app_versionCode: String by project
val app_versionName: String by project
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

    // koin
    implementation("io.insert-koin:koin-core:$ver_koin")

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$ver_coroutines")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$ver_coroutines")

    // todo upgrade ktor - sec vuln
    implementation("io.ktor:ktor-client-core:$ver_ktor")
    implementation("io.ktor:ktor-client-cio:$ver_ktor")

    implementation("org.apache.xmlgraphics:batik-transcoder:$ver_batik")
    implementation("com.russhwolf:multiplatform-settings:$ver_multiplatform_settings")

    testImplementation("junit:junit:$ver_junit")
    testImplementation("io.insert-koin:koin-test:$ver_koin")
    testImplementation("io.insert-koin:koin-test-junit4:$ver_koin")
    testImplementation("io.mockk:mockk:$ver_mockk")
    testImplementation("app.cash.turbine:turbine:$ver_turbine")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines")

    implementation("uk.co.caprica:vlcj:4.8.2")

}

compose.desktop {
    application {
        mainClass = "uk.co.sentinelweb.cuer.hub.main.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "test"
            packageVersion = "1.0.0"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = ver_jvm
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = ver_jvm
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
                val versionCode: Int = $app_versionCode
                val version: String = "$app_versionName"
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
