plugins {
    id("example-base")
    id("spotless-java")
}

dependencies {
    implementation(project(":spring:spring-webmvc"))
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.mybatis.spring.boot.starter)
    runtimeOnly(libs.postgresql.jdbc)
    developmentOnly(libs.spring.boot.docker.compose)
}
