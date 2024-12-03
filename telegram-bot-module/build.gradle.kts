plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"


}

repositories {
    mavenCentral()
}

dependencies {
    implementation("eu.vendeli:telegram-bot:7.5.0")
    ksp("eu.vendeli:ksp:7.5.0")
    implementation(libs.io.ktor.client.cio)
    implementation(project(":redis-client-module"))
}
