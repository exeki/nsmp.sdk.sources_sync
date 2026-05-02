plugins {
    kotlin("jvm") version "2.3.0" apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
}

group = "ru.kazantsev.nsmp.sdk.sources_sync"
version = "1.7.5"

subprojects {

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/exeki/*")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
