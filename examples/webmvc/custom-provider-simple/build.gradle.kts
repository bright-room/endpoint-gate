plugins {
    id("example-base")
    id("spotless-java")
}

dependencies {
    implementation(project(":spring:spring-webmvc"))
    implementation(libs.spring.boot.starter.webmvc)
}
