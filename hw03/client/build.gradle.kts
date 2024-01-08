plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.compnets.hw3"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.github.ajalt.clikt:clikt:3.5.0")
    // https://mvnrepository.com/artifact/com.athaydes.rawhttp/rawhttp-core
    implementation("com.athaydes.rawhttp:rawhttp-core:2.5.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}