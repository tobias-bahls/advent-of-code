import com.diffplug.gradle.spotless.SpotlessExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21" apply false
    id("com.diffplug.spotless") version "6.12.0"
}

configure<SpotlessExtension> { kotlinGradle { ktfmt().dropboxStyle() } }

allprojects {
    group = "de.tobias"
    version = "NONE"

    apply(plugin = "com.diffplug.spotless")

    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

    configure<SpotlessExtension> {
        kotlin { ktfmt().dropboxStyle() }
        kotlinGradle { ktfmt().dropboxStyle() }
    }
}
