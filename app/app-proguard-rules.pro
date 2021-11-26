# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# dont obfuscate for now
-dontobfuscate

# https://github.com/Kotlin/kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep class uk.co.sentinelweb.cuer.domain.**
-keep class uk.co.sentinelweb.cuer.net.youtube.videos.dto.**
-keep class uk.co.sentinelweb.cuer.net.pixabay.dto.**
-keep,includedescriptorclasses class uk.co.sentinelweb.cuer.domain.**$$serializer { *; }
-keepclassmembers class uk.co.sentinelweb.cuer.domain.** {
    *** Companion;
}
-keepclasseswithmembers class uk.co.sentinelweb.cuer.domain.** {
    kotlinx.serialization.KSerializer serializer(...);
}