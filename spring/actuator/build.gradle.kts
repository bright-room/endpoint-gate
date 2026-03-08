plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

description = "endpoint-gate spring-actuator module"

dependencies {
    api(projects.springCore)
    implementation(projects.reactiveCore)
    implementation(libs.spring.boot.starter.actuator)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.reactor.test)
}
