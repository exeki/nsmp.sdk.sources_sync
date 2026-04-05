plugins {
    id("nsmp_sdk_sources_sync")
}

{{SDK_CONFIGURATION}}

tasks.register("printImplementationDeps") {
    doLast {
        configurations.getByName("implementation").dependencies.forEach {
            println("${it.group}:${it.name}:${it.version}")
        }
    }
}
