plugins {
    id("example-base")
    id("spotless-java")
}

dependencies {
    implementation(project(":spring:spring-webflux"))
    implementation(libs.spring.boot.starter.webflux)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.boot:spring-boot-docker-compose")
}
