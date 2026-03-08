enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "endpoint-gate"

include("core")
include("reactive-core")
include("spring:core")
include("spring:webmvc")
include("spring:webflux")
include("spring:actuator")

// ディレクトリパス → artifactId のマッピング
project(":spring:core").name = "spring-core"
project(":spring:webmvc").name = "spring-webmvc"
project(":spring:webflux").name = "spring-webflux"
project(":spring:actuator").name = "spring-actuator"

includeBuild("gradle-scripts")
