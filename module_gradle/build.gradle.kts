plugins {
    kotlin("jvm")
    id("java-gradle-plugin")
    id("maven-publish")
}

kotlin {
    jvmToolchain(21)
}

gradlePlugin {
    plugins {
        create("sourcesSyncPlugin") {
            id = "nsmp_sdk_sources_sync"
            version = project.version
            group = project.group
            implementationClass = "ru.kazantsev.nsmp.sdk.gradle_plugin.Plugin"
        }
    }
}

dependencies {
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    implementation(project(":core"))
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
}

tasks {
    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.name
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/exeki/nsd.sdk.gradle_plugin")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
