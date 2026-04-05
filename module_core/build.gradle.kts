plugins {
    kotlin("jvm")
    id("java-library")
    id("maven-publish")
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/exeki/nsmp.sdk.sources_sync")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api("ru.kazantsev.nsmp:basic_api_connector:1.5.3")
    api("org.slf4j:slf4j-api:2.0.17")
    testImplementation(kotlin("test"))
}
