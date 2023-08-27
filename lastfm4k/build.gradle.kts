plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "org.gravidence"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.http4k:http4k-core:4.40.2.0")
    implementation("org.slf4j:jcl-over-slf4j:2.0.5")
    implementation("org.slf4j:jul-to-slf4j:2.0.5")
    implementation("org.slf4j:log4j-over-slf4j:2.0.5")

    testImplementation(kotlin("test"))
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

kotlin {
    jvmToolchain(17)

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}