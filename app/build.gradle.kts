import java.nio.file.Files
import java.nio.charset.StandardCharsets

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.compose")
}

val ver_firebase_bom: String by project
val ver_firebase_ui: String by project

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    namespace = libs.versions.app.applicationId.get()
    useLibrary("android.test.runner")
    useLibrary("android.test.base")
    useLibrary("android.test.mock")

    android.buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = libs.versions.app.applicationId.get()
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val ytKey = getPropertyOrDefault("CUER_YOUTUBE_API_KEY", "CUER_YOUTUBE_API_KEY_DEFAULT")
        buildConfigField("String", "youtubeApiKey", "\"$ytKey\"")
        val pixKey = getPropertyOrDefault("CUER_PIXABAY_API_KEY", "CUER_PIXABAY_API_KEY_DEFAULT")
        buildConfigField("String", "pixabayApiKey", "\"$pixKey\"")
        val isBgPlay = getPropertyOrDefault("CUER_BG_PLAY", "true")
        buildConfigField("boolean", "cuerBackgroundPlay", isBgPlay)
        val isRemote = getPropertyOrDefault("CUER_REMOTE_ENABLED", "true")
        buildConfigField("boolean", "cuerRemoteEnabled", isRemote)
    }

    signingConfigs {
        create("release") {
            // for bundleRelease
//            keyAlias "$CUER_UPLOAD_KEY_ALIAS"
//            keyPassword "$CUER_UPLOAD_PASSWORD"
//            storeFile file("$CUER_UPLOAD_KEY_FILE")
//            storePassword "$CUER_UPLOAD_PASSWORD"
            // for assembleRelease
            val signingKeyAlias = getPropertyOrDefault("CUER_SIGNING_KEY_ALIAS", "CUER_SIGNING_KEY_ALIAS_DEFAULT")
            keyAlias = signingKeyAlias
            val signingPassword = getPropertyOrDefault("CUER_SIGNING_PASSWORD", "CUER_SIGNING_PASSWORD_DEFAULT")
            keyPassword = signingPassword
            val signingFileAlias = getPropertyOrDefault("CUER_SIGNING_KEY_FILE", "CUER_SIGNING_KEY_FILE_DEFAULT")
            storeFile = file(signingFileAlias)
            storePassword = signingPassword
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "app-proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvm.get()
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.time.ExperimentalTime",
            "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi"
        )
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    sourceSets {
        getByName("testDebug").java.srcDir("src/sharedTest/kotlin")
        getByName("androidTest").java.srcDir("src/sharedTest/kotlin")
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":net"))
    implementation(project(":remote"))
    implementation(project(":database"))// todo: remove get through shared
    implementation(project(":domain"))

    implementation(fileTree("libs") {
        include("*.jar")
    })

    // deps
    implementation(libs.kotlinStdLib)
//    implementation("androidx.appcompat:appcompat:$ver_appcompat")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.lifecycleViewModelKtx)
    implementation(libs.material)
    implementation(libs.constraintLayout)
    implementation(libs.lifecycleExtensions)
    implementation(libs.kotlinxDatetime)
    implementation(libs.recyclerView)
    implementation(libs.swiperefreshlayout)
    implementation(libs.preferenceKtx)

    // WorkManager
    implementation(libs.workRuntimeKtx)

    // Compose
    implementation(libs.composeThemeAdapter)
    implementation(compose.animation)
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.ui)
    implementation(compose.components.resources)
    implementation(libs.composeUiToolingPreview)
    implementation(libs.composeUiTooling)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.activity.compose)

    // mvikotlin
    debugImplementation(libs.mvikotlinAndroidDebug)
    releaseImplementation(libs.mvikotlinAndroid)
    implementation(libs.mvikotlin)
    implementation(libs.mvikotlinLogging)

    // navigation
    implementation(libs.navigationFragmentKtx)
    implementation(libs.navigationUiKtx)
    implementation(libs.navigationFragment)
    implementation(libs.navigationUi)

    // koin
    implementation(libs.koinCore)
    implementation(libs.koinAndroid)

    // debug
    implementation(libs.stetho)
    implementation(libs.stethoOkhttp3)

    // youtube player
    implementation(libs.androidYouTubePlayer)
    implementation(libs.chromecastSender)
    implementation(libs.mediarouter)
    implementation(libs.media)

    // exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)

    // firebase
//    implementation(platform(libs.firebaseBom))
    implementation(project.dependencies.enforcedPlatform(libs.firebaseBom.get()))
    implementation(libs.firebaseStorageKtx)
    implementation(libs.firebaseCrashlyticsKtx)
    implementation(libs.firebaseAnalyticsKtx)
    implementation(libs.firebaseUiStorage)

    // serialization
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)

    // glide
    implementation(libs.glide)
    kapt(libs.glideCompiler)
    implementation(libs.accompanistGlide)
    implementation(libs.accompanistDrawablepainter)

    // coil
    implementation(libs.coilCompose)

    // ktor
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientCio)

    // splash
    implementation(libs.coreSplashscreen)

    // test implementations
    debugImplementation(libs.androidxFragmentTest)

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")

    testImplementation(libs.junit)
    testImplementation(libs.koinTest)
    testImplementation(libs.truth)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.mockitoInline)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockk)
    testImplementation(libs.jfixture)
    testImplementation(libs.kotlinFixture)
    testImplementation(libs.kotlinxCoroutinesTest)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidxTestJUnit)
    testImplementation(libs.runner)
    testImplementation(libs.rules)
    testImplementation(libs.coreTesting)
    testImplementation(libs.espressoCore)
    testImplementation(libs.espressoContrib)
    testImplementation(libs.espressoIntents)
    testImplementation(libs.turbine)

    // androidTestImplementation
    androidTestImplementation(libs.koinTest)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockitoCore)
    androidTestImplementation(libs.mockitoAndroid)
    androidTestImplementation(libs.mockitoKotlin)
    androidTestImplementation(libs.jfixture)
    androidTestImplementation(libs.androidxTestJUnit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.espressoContrib)
    androidTestImplementation(libs.espressoIntents)
    androidTestImplementation(libs.robolectric)
    // optional - WorkManager Test helpers
    //androidTestImplementation "androidx.work:work-testing:$ver_workmanager"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs += listOf("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

fun getPropertyOrDefault(key: String, defaultValue: String): String {
    return project.findProperty(key) as? String ?: defaultValue
}

tasks.register("generateNetworkSecurityConfig") {

    // Destination directory and file name
    val outputDir = projectDir.resolve("src/main/res/xml")
    val outputFile = outputDir.resolve("network_security_config.xml")

    doLast {
        // Ensure the output directory exists
        if (!Files.exists(outputDir.toPath())) {
            Files.createDirectories(outputDir.toPath())
        }

        // Create header and footer for the XML
        val header = """
            <network-security-config>
                <domain-config cleartextTrafficPermitted="true">
        """.trimIndent()

        val footer = """
                </domain-config>
            </network-security-config>
        """.trimIndent()

        // Generate IP entries for 192.168.0.x and 192.168.1.x ranges
        val ipEntries = StringBuilder()
        for (i in 1..254) {
            ipEntries.append("        <domain includeSubdomains=\"false\">192.168.0.$i</domain>\n")
        }
        for (i in 1..254) {
            ipEntries.append("        <domain includeSubdomains=\"false\">192.168.1.$i</domain>\n")
        }

        // Combine header, entries, and footer
        val fullContent = header + "\n" + ipEntries.toString() + footer

        // Write to file
        Files.write(outputFile.toPath(), fullContent.toByteArray(StandardCharsets.UTF_8))
        println("Generated network_security_config.xml with entries for 192.168.0.x and 192.168.1.x ranges.")
    }
}

tasks.named("preBuild") {
    dependsOn("generateNetworkSecurityConfig")
}
