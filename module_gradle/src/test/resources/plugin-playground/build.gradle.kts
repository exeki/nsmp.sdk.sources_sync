plugins {
    id("java")
    id("nsmp_sdk_sources_sync") version "2.0.8"

}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

repositories {
    mavenCentral()
    mavenLocal()
}

sdk {
    setSendFilePath("src/main/groovy/console.groovy")
    setInstallation("EXEKI1")
}
