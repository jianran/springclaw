import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    `java-library`
    `maven-publish`
    signing
    id("io.freefair.lombok") version "8.11" apply false
    id("org.springframework.boot") version "3.4.5" apply false
    id("com.github.ben-manes.versions") version "0.52.0" apply false
}

extra.apply {
    set("springBoot", "3.4.5")
    set("springAi", "1.0.0-M6")
    set("lombok", "1.18.36")
    set("reactor_version", "3.7.0")
    set("slf4j", "2.0.17")
    set("junit", "5.11.4")
    set("mockk", "1.13.16")
    set("jackson", "2.18.3")
}

allprojects {
    group = "io.springclaw"
    version = findProperty("projectVersion") as? String ?: "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven { url = uri("https://repo.spring.io/milestone") }
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.freefair.lombok")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        val ext = rootProject.extra
        testImplementation("org.junit.jupiter:junit-jupiter:${ext["junit"] as String}")
        testImplementation("io.mockk:mockk:${ext["mockk"] as String}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }
}
