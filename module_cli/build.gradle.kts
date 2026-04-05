plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.4.1"
    id("maven-publish")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "ru.kazantsev.nsmp.sdk.MainKt"
    }
}

tasks.jar {
    enabled = false
}

dependencies {
    implementation(project(":core"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.slf4j:slf4j-api:2.0.17")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.shadowJar)
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
