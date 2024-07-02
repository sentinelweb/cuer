// todo convert .kts
apply plugin: 'com.android.application'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'
apply plugin: 'kotlinx-serialization'
apply plugin: 'com.google.gms.google-services'
apply plugin: "androidx.navigation.safeargs.kotlin"
apply plugin: 'com.google.firebase.crashlytics'
apply plugin: 'org.jetbrains.compose'

android {
    compileSdkVersion libs.versions.android.compileSdk
    namespace app_applicationId
    useLibrary 'android.test.runner'
    useLibrary 'android.test.base'
    useLibrary 'android.test.mock'

    android.buildFeatures.buildConfig true

    defaultConfig {
        applicationId app_applicationId
        minSdkVersion libs.versions.android.minSdk
        targetSdkVersion libs.versions.android.targetSdk
        versionCode app_versionCode as int
        versionName app_versionName

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        def ytKey = getPropertyOrDefault('CUER_YOUTUBE_API_KEY', 'CUER_YOUTUBE_API_KEY_DEFAULT')
        buildConfigField "String", "youtubeApiKey", "\"$ytKey\""
        def pixKey = getPropertyOrDefault('CUER_PIXABAY_API_KEY', 'CUER_PIXABAY_API_KEY_DEFAULT')
        buildConfigField "String", "pixabayApiKey", "\"$pixKey\""
        def isBgPlay = getPropertyOrDefault('CUER_BG_PLAY', 'true')
        buildConfigField "boolean", "cuerBackgroundPlay", "$isBgPlay"
        def isRemote = getPropertyOrDefault('CUER_REMOTE_ENABLED', 'true')
        buildConfigField "boolean", "cuerRemoteEnabled", "$isRemote"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += ["room.schemaLocation": "$projectDir/schemas".toString()]
            }
//            compileOptions {
//                arguments += ["-Xdebug":true]
//            }
        }
    }

    signingConfigs {
        release {
            // for bundleRelease
//            keyAlias "$CUER_UPLOAD_KEY_ALIAS"
//            keyPassword "$CUER_UPLOAD_PASSWORD"
//            storeFile file("$CUER_UPLOAD_KEY_FILE")
//            storePassword "$CUER_UPLOAD_PASSWORD"
            // for assembleRelease
            def signingKeyAlias = getPropertyOrDefault('CUER_SIGNING_KEY_ALIAS', 'CUER_SIGNING_KEY_ALIAS_DEFAULT')
            keyAlias "$signingKeyAlias"
            def signingPassword = getPropertyOrDefault('CUER_SIGNING_PASSWORD', 'CUER_SIGNING_PASSWORD_DEFAULT')
            keyPassword "$signingPassword"
            def signingFileAlias = getPropertyOrDefault('CUER_SIGNING_KEY_FILE', 'CUER_SIGNING_KEY_FILE_DEFAULT')
            storeFile file("$signingFileAlias")

            storePassword "$signingPassword"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix '.debug'
            versionNameSuffix '-DEBUG'
        }

        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'app-proguard-rules.pro'
            signingConfig signingConfigs.release

        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = ver_jvm
        freeCompilerArgs += [
                // bypass compose kotlin version compatability check
                // https://stackoverflow.com/questions/65545018/where-can-i-put-the-suppresskotlinversioncompatibilitycheck-flag
//                "-P",
//                "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi"
        ]
    }

    composeOptions {
        kotlinCompilerVersion ver_kotlin
        kotlinCompilerExtensionVersion ver_compose_compiler
        // todo move to libs.versions.toml (getting a null error)
//        kotlinCompilerExtensionVersion libs.versions.composeCompiler
    }

    buildFeatures {
        viewBinding true
        compose true
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        androidTest.assets.srcDirs += files("$projectDir/schemas".toString())
        testDebug {
            java.srcDir 'src/sharedTest/kotlin'
        }
        androidTest {
            java.srcDir 'src/sharedTest/kotlin'
        }
    }

    testOptions {
        unitTests.returnDefaultValues = true
        unitTests.includeAndroidResources = true
    }

    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/LICENSE.txt'
        exclude 'META-INF/license.txt'
        exclude 'META-INF/NOTICE'
        exclude 'META-INF/NOTICE.txt'
        exclude 'META-INF/notice.txt'
        exclude 'META-INF/ASL2.0'
        exclude 'META-INF/INDEX.LIST'
    }
}

dependencies {
    implementation project(":shared")
//    implementation project(":sharedUi")
    implementation project(':net')
    implementation project(":remote")
    implementation project(":database")// todo: remove get through shared
    implementation project(":domain")

    implementation fileTree(dir: 'libs', include: ['*.jar'])

    // deps
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$ver_kotlin"
    implementation "androidx.appcompat:appcompat:$ver_appcompat"
    implementation "androidx.fragment:fragment:$ver_fragment_ktx"
    implementation "androidx.fragment:fragment-ktx:$ver_fragment_ktx"
    implementation "androidx.core:core:$ver_androidx_core"
    implementation "androidx.core:core-ktx:$ver_androidx_core"
    implementation "androidx.annotation:annotation:$ver_androidx_annotation"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$ver_lifecycle_viewmodel_ktx"
    implementation "com.google.android.material:material:$ver_material"
    implementation "androidx.constraintlayout:constraintlayout:$ver_constraintlayout"
    implementation "androidx.lifecycle:lifecycle-extensions:$ver_lifecycle_extensions"
    implementation "org.jetbrains.kotlinx:kotlinx-datetime:$ver_kotlinx_datetime"
    implementation "androidx.recyclerview:recyclerview:$ver_recyclerview"
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:$ver_swipe_refresh"
    implementation "androidx.preference:preference-ktx:$ver_preference"

    // WorkManager
    // Kotlin + coroutines
    implementation "androidx.work:work-runtime-ktx:$ver_workmanager"

    // Compose
//    implementation "com.google.android.material:compose-theme-adapter:$ver_compose_theme_adapter"
    implementation libs.composeThemeAdapter
    implementation compose.animation
    implementation compose.runtime
    implementation compose.foundation
    implementation compose.material
    implementation compose.ui
    implementation compose.components.resources
//    implementation compose.components.uiToolingPreview
    implementation libs.composeUiToolingPreview

    // mvikotlin
    debugImplementation "com.arkivanov.mvikotlin:mvikotlin-android-debug:$ver_mvikotlin"
    releaseImplementation "com.arkivanov.mvikotlin:mvikotlin-android:$ver_mvikotlin"
    implementation "com.arkivanov.mvikotlin:mvikotlin-main:$ver_mvikotlin"
    // for debugging only remove
    implementation "com.arkivanov.mvikotlin:mvikotlin-logging:$ver_mvikotlin"

    //navigation
    implementation "androidx.navigation:navigation-fragment-ktx:$ver_navigation"
    implementation "androidx.navigation:navigation-ui-ktx:$ver_navigation"
    implementation "androidx.navigation:navigation-fragment:$ver_navigation"
    implementation "androidx.navigation:navigation-ui:$ver_navigation"

    // koin
    implementation "io.insert-koin:koin-core:$ver_koin"
    implementation "io.insert-koin:koin-android:$ver_koin"

    // debug
    implementation "com.facebook.stetho:stetho:$ver_stetho"
    implementation "com.facebook.stetho:stetho-okhttp3:$ver_stetho"

    // https://github.com/PierfrancescoSoffritti/Android-YouTube-Player#1-youtubeplayerviewgetyoutubeplayerwhenready
    implementation "com.pierfrancescosoffritti.androidyoutubeplayer:core:$ver_android_youtube_player"
    // cc sender
    implementation "com.pierfrancescosoffritti.androidyoutubeplayer:chromecast-sender:$ver_chromecast_sender"
    // this is not needed to use the library, but it provides the very useful MediaRouteButton.
    implementation "androidx.mediarouter:mediarouter:$ver_mediarouter"

    implementation "androidx.media:media:$ver_androidx_media"

    // FIREBASE
    // Import the BoM for the Firebase platform
    implementation platform("com.google.firebase:firebase-bom:$ver_firebase_bom")
    // Declare the dependency for the Cloud Storage library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation 'com.google.firebase:firebase-storage-ktx'
    implementation 'com.google.firebase:firebase-crashlytics-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'

    implementation "com.firebaseui:firebase-ui-storage:$ver_firebase_ui"

    // korlin serialization
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-core:$ver_kotlinx_serialization_core"
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:$ver_kotlinx_serialization_core"

    // glide
    implementation "com.github.bumptech.glide:glide:$ver_glide"
    annotationProcessor "com.github.bumptech.glide:compiler:$ver_glide"
    kapt "com.github.bumptech.glide:compiler:$ver_glide"
    // https://google.github.io/accompanist/glide/
    implementation ("com.google.accompanist:accompanist-glide:$ver_glide_accompanist"){
        exclude group:"androidx.compose.runtime"
        exclude group:"androidx.compose.ui"
        exclude group:"androidx.compose.foundation"
        exclude group:"androidx.compose.material"
        exclude group:"com.google.android.material"
    }
    implementation "com.google.accompanist:accompanist-drawablepainter:$ver_accompanist"
    // coil
    implementation("io.coil-kt:coil-compose:$ver_coil")

    // ktor
    implementation "io.ktor:ktor-client-core:$ver_ktor"
    implementation "io.ktor:ktor-client-cio:$ver_ktor"

    // splash screen
    implementation "androidx.core:core-splashscreen:$ver_splash"

    debugImplementation("androidx.fragment:fragment-testing:$ver_androidx_fragment_test") {
        exclude group: "androidx.test"
    }

    // leak canary
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.9.1'

    testImplementation "junit:junit:$ver_junit"
    testImplementation "io.insert-koin:koin-test:$ver_koin"
    testImplementation "io.insert-koin:koin-test-junit4:$ver_koin"
    testImplementation "io.mockk:mockk:$ver_mockk"
    testImplementation "com.google.truth:truth:$ver_truth"
    testImplementation "org.mockito:mockito-core:$ver_mockito"
    testImplementation "org.mockito:mockito-inline:$ver_mockito_inline"
    testImplementation "com.nhaarman.mockitokotlin2:mockito-kotlin:$ver_mockito_kotlin"
    testImplementation "com.flextrade.jfixture:jfixture:$ver_jfixture"// todo remove
    testImplementation "com.appmattus.fixture:fixture:$ver_kotlin_fixture"
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$ver_coroutines"
    testImplementation "org.robolectric:robolectric:$ver_robolectric"
    testImplementation "androidx.test.ext:junit:$ver_androidx_test_junit"
    testImplementation "androidx.test:core:$ver_androidx_test"
    testImplementation "androidx.test:runner:$ver_androidx_test"
    testImplementation "androidx.test:rules:$ver_androidx_test"
    testImplementation "android.arch.core:core-testing:$ver_android_arch_test"
    testImplementation "androidx.test.espresso:espresso-core:$ver_espresso_core"
    testImplementation "androidx.test.espresso:espresso-contrib:$ver_espresso_core"
    testImplementation "androidx.test.espresso:espresso-intents:$ver_espresso_core"
    testImplementation "app.cash.turbine:turbine:$ver_turbine"

    androidTestImplementation "io.insert-koin:koin-test:$ver_koin"
    androidTestImplementation "com.google.truth:truth:$ver_truth"
    androidTestImplementation "org.mockito:mockito-core:$ver_mockito"
    androidTestImplementation "org.mockito:mockito-android:$ver_mockito"
    androidTestImplementation "com.nhaarman.mockitokotlin2:mockito-kotlin:$ver_mockito_kotlin"
    androidTestImplementation "com.flextrade.jfixture:jfixture:$ver_jfixture"
    androidTestImplementation "androidx.test.ext:junit:$ver_androidx_test_junit"
    androidTestImplementation "androidx.test:core:$ver_androidx_test"
    androidTestImplementation "androidx.test:runner:$ver_androidx_test"
    androidTestImplementation "androidx.test:rules:$ver_androidx_test"
    androidTestImplementation "androidx.test.espresso:espresso-core:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso:espresso-contrib:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso:espresso-intents:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso:espresso-accessibility:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso:espresso-web:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso.idling:idling-concurrent:$ver_espresso_core"
    androidTestImplementation "androidx.test.espresso:espresso-idling-resource:$ver_espresso_core"
    androidTestImplementation "org.robolectric:annotations:$ver_robolectric"

    // optional - WorkManager Test helpers
    //androidTestImplementation "androidx.work:work-testing:$ver_workmanager"
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {
    kotlinOptions {
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }
}

def getPropertyOrDefault(String key, String defaultValue) {
    def val = project.property(key)
    return val ?: defaultValue
}
