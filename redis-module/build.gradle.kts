plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.io.lettuce.core)
    implementation(libs.io.ktor.server.core)
    implementation(libs.io.ktor.server.netty)
    implementation(libs.io.ktor.server.content.negotiation)
    implementation(libs.io.ktor.serialization.kotlinx.json)
    implementation(project(":common-module"))
}
