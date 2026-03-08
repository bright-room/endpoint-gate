plugins {
    id("spring-boot-starter")
    id("publish-plugin")
    id("spotless-java")
    id("integration-test")
}

description = "endpoint-gate spring-webmvc module"

dependencies {
    api(projects.springCore)
    implementation(libs.spring.boot.starter.webmvc)
    testImplementation(libs.spring.boot.starter.test)
}
