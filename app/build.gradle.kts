// todo convert .kts
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
// todo move all to libs.versions.toml
//val ver_jvm: String by project
val ver_fragment_ktx: String by project
val ver_androidx_annotation: String by project
val ver_lifecycle_viewmodel_ktx: String by project
val ver_material: String by project
val ver_constraintlayout: String by project
val ver_lifecycle_extensions: String by project
val ver_kotlinx_datetime: String by project
val ver_recyclerview: String by project
val ver_swipe_refresh: String by project
val ver_preference: String by project
val ver_workmanager: String by project
val ver_mvikotlin: String by project
val ver_navigation: String by project
val ver_koin: String by project
val ver_stetho: String by project
val ver_android_youtube_player: String by project
val ver_chromecast_sender: String by project
val ver_mediarouter: String by project
val ver_androidx_media: String by project
val ver_firebase_bom: String by project
val ver_firebase_ui: String by project
val ver_kotlinx_serialization_core: String by project
val ver_glide: String by project
val ver_glide_accompanist: String by project
val ver_accompanist: String by project
val ver_coil: String by project
val ver_ktor: String by project
val ver_splash: String by project
// test
val ver_androidx_fragment_test: String by project
val ver_mockk: String by project
val ver_truth: String by project
val ver_mockito: String by project
val ver_mockito_inline: String by project
val ver_mockito_kotlin: String by project
val ver_jfixture: String by project
val ver_kotlin_fixture: String by project
val ver_coroutines: String by project
val ver_robolectric: String by project
val ver_androidx_test: String by project
val ver_espresso_core: String by project
val ver_android_arch_test: String by project
val ver_turbine: String by project
val ver_androidx_test_junit: String by project

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
        buildConfigField("boolean", "cuerBackgroundPlay", "$isBgPlay")
        val isRemote = getPropertyOrDefault("CUER_REMOTE_ENABLED", "true")
        buildConfigField("boolean", "cuerRemoteEnabled", "$isRemote")
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

    // firebase
    implementation(libs.firebaseBom)
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
