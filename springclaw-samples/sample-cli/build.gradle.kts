plugins {
    `java-library`
    id("org.springframework.boot")
    id("application")
}

dependencies {
    implementation(project(":springclaw-spring-boot"))
    implementation(project(":springclaw-gateway"))
    implementation(project(":springclaw-cli"))
    implementation(project(":springclaw-tools"))
    implementation(project(":springclaw-memory"))
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:${rootProject.extra["springAi"] as String}")
    runtimeOnly("com.h2database:h2:2.3.232")
}

application {
    mainClass = "com.springclaw.sample.cli.CliSampleApplication"
}
