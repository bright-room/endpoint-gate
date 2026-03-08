plugins {
    id("example-base")
    id("spotless-java")
}

dependencies {
    implementation(project(":spring:spring-webmvc"))
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.thymeleaf)

    // Jackson 3 XML support
    implementation(libs.jackson.xml)
}
