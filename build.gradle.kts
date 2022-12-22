import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
    id("com.diffplug.spotless") version "6.12.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.18" apply false
    id("com.github.ben-manes.versions") version "0.44.0" apply false
}

configure<SpotlessExtension> { kotlinGradle { ktfmt().dropboxStyle() } }

allprojects {
    group = "de.tobias"
    version = "NONE"

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
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

    configure<SpotlessExtension> {
        kotlin { ktfmt().dropboxStyle() }
        kotlinGradle { ktfmt().dropboxStyle() }
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:23.1.0")

        implementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
        implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    }
}
