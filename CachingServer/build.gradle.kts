plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.8")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("de.chasenet.derpibooru.MainKt")
}