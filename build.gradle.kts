import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.diffplug.spotless") version "6.12.0"
    application
}

group = "de.tobias"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test")) }

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

configure<SpotlessExtension> {
    kotlin { ktfmt().dropboxStyle() }
    kotlinGradle { ktfmt().dropboxStyle() }
}
