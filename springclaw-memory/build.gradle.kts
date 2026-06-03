plugins {
    `java-library`
}

dependencies {
    api(project(":springclaw-core"))
    api(project(":springclaw-spring-boot"))
    api("org.springframework.ai:spring-ai-core:${rootProject.extra["springAi"] as String}")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${rootProject.extra["springBoot"] as String}")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:${rootProject.extra["springBoot"] as String}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${rootProject.extra["jackson"] as String}")
}
