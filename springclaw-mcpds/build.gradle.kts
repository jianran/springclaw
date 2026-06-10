plugins {
    `java-library`
}

dependencies {
    val ext = rootProject.extra
    api(project(":springclaw-core"))
    api("org.springframework.ai:spring-ai-core:${ext["springAi"] as String}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${ext["springBoot"] as String}")
    implementation("org.springframework.boot:spring-boot-starter-webflux:${ext["springBoot"] as String}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${ext["jackson"] as String}")
    api("io.projectreactor:reactor-core:${ext["reactor_version"] as String}")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${ext["springBoot"] as String}")
    testImplementation("org.mockito:mockito-core:5.14.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
