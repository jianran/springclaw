plugins {
    `java-library`
}

dependencies {
    api(project(":springclaw-core"))
    api("org.springframework.ai:spring-ai-core:${rootProject.extra["springAi"] as String}")
    api("org.springframework.boot:spring-boot-autoconfigure:${rootProject.extra["springBoot"] as String}")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${rootProject.extra["springBoot"] as String}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${rootProject.extra["jackson"] as String}")
}
