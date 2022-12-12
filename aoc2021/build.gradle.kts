plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")

    application
}

dependencies {
    implementation(project(":utils"))
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    implementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
}
