plugins {
    kotlin("jvm")
}

group = "uk.co.sentinlweb.kmpwiz"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

tasks.register<JavaExec>("convertDrawablesToSVG") {
    doFirst {
        println("Running script...")
    }
    // Replace 'your.package.MainKt' with the fully qualified class name
    mainClass = "ConvertDrawablesKt"
    classpath = sourceSets.main.get().runtimeClasspath

    doLast {
        println("Script has run.")
    }
}

