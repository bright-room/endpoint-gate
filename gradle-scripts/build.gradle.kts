plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugins.spring.boot.map { "org.springframework.boot:spring-boot-gradle-plugin:${it.version}" })
    implementation(libs.plugins.spring.dependency.management.map { "io.spring.gradle:dependency-management-plugin:${it.version}" })
    implementation(libs.plugins.spotless.map { "com.diffplug.spotless:spotless-plugin-gradle:${it.version}" })
    implementation(libs.plugins.nexus.publish.map { "io.github.gradle-nexus:publish-plugin:${it.version}" })
}
