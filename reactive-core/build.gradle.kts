plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
}

description = "endpoint-gate reactive-core module (Reactor dependency)"

dependencies {
    api(projects.core)
    implementation(libs.reactor.core)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.reactor.test)
}
