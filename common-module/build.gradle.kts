plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

group = "org.example"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.ktor.server.core)
}

kotlin {
    jvmToolchain(21)
}