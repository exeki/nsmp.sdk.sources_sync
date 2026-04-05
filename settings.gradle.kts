plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "sources_sync"
include("core", "cli", "gradle")
project(":core").projectDir = file("module_core")
project(":cli").projectDir = file("module_cli")
project(":gradle").projectDir = file("module_gradle")
