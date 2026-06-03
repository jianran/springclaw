plugins {
    `java-library`
}

dependencies {
    api(libs.slf4j.api)
    api("io.projectreactor:reactor-core:${rootProject.extra["reactor_version"] as String}")
}
