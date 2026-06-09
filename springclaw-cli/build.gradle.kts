plugins {
    `java-library`
    id("org.springframework.boot")
    id("application")
}

dependencies {
    api(project(":springclaw-core"))
    api(project(":springclaw-spring-boot"))
    api(project(":springclaw-gateway"))
    api("org.springframework.boot:spring-boot-starter-webflux:${rootProject.extra["springBoot"] as String}")
    api("org.springframework.ai:spring-ai-core:${rootProject.extra["springAi"] as String}")
    // JLine for rich terminal interaction
    api("org.jline:jline:3.27.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:${rootProject.extra["jackson"] as String}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${rootProject.extra["jackson"] as String}")
}

application {
    mainClass = "com.springclaw.cli.CliApplication"
}
