import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.jetbrains.lets-plot:lets-plot-common:3.2.0")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.4.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register<DiagramGenerationTask>("generateDiagrams") {
    destinationDirectory.set(File(projectDir, "diagrams"))
}