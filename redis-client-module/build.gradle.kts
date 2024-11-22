plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.devtools.ksp") version "2.0.21-1.0.26"
    id("eu.vendeli.telegram-bot") version "7.4.1"

}


repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.ktor.client.core)
    implementation(libs.io.ktor.client.cio)
    implementation(libs.io.ktor.client.websockets)
    implementation(libs.io.ktor.client.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(libs.io.ktor.client.logging)
    implementation(project(":notifications-api"))
    implementation("eu.vendeli:telegram-bot:7.4.1")
    ksp("eu.vendeli:ksp:7.4.1")
}
kotlin {
    jvmToolchain(21)
}