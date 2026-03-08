plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
}

description = "endpoint-gate core module (Pure Java / JDK only)"

dependencies {
    testImplementation(libs.spring.boot.starter.test)
}
