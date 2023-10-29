import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.diffplug.spotless") version "6.22.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18" apply false
    id("com.github.ben-manes.versions") version "0.49.0" apply false
}

allprojects {
    group = "de.tobias"
    version = "NONE"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "com.github.ben-manes.versions")

    repositories { mavenCentral() }

    fun isNonStable(version: String): Boolean {
        val stableKeyword =
            listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
    tasks.withType<DependencyUpdatesTask> { rejectVersionIf { isNonStable(candidate.version) } }

    kotlin { jvmToolchain(17) }

    configure<SpotlessExtension> {
        kotlin { ktfmt().dropboxStyle() }
        kotlinGradle { ktfmt().dropboxStyle() }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.0.1")

        implementation("io.kotest:kotest-assertions-core-jvm:5.7.2")
        implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    }
}
