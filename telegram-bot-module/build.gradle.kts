plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"


}

repositories {
    mavenCentral()
}

dependencies {
    implementation("eu.vendeli:telegram-bot:7.5.0")
    ksp("eu.vendeli:ksp:7.5.0")
    implementation(project(":redis-client-module"))
}
