pluginManagement {
    includeBuild("gradle-scripts")
    repositories {
        mavenCentral()
        gradlePluginPortal()
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
