plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")

    application
}

dependencies { implementation(project(":utils")) }
