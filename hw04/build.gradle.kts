import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    application
}

group = "mkn.compnets.hw4"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    // https://mvnrepository.com/artifact/com.athaydes.rawhttp/rawhttp-core
    implementation("com.athaydes.rawhttp:rawhttp-core:2.5.2")
    // https://mvnrepository.com/artifact/commons-httpclient/commons-httpclient
    implementation("commons-httpclient:commons-httpclient:3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}