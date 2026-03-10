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
includeBuild("gradle-scripts")

// ###########################################
// # Library modules
// ###########################################
include(
    "core",
    "reactive-core",
    "spring:core",
    "spring:webmvc",
    "spring:webflux",
    "spring:actuator",
)

project(":spring:core").name = "spring-core"
project(":spring:webmvc").name = "spring-webmvc"
project(":spring:webflux").name = "spring-webflux"
project(":spring:actuator").name = "spring-actuator"

// ###########################################
// # Example modules
// ###########################################
include(
    "examples:webmvc:basic-usage",
    "examples:webmvc:fail-behavior",
    "examples:webmvc:custom-provider-database",
    "examples:webmvc:custom-provider-simple",
    "examples:webmvc:error-handling",
    "examples:webmvc:rollout",
    "examples:webmvc:functional-endpoint",
    "examples:webmvc:actuator-endpoint",
    "examples:webmvc:health-indicator",
    "examples:webmvc:custom-rollout-strategy",
    "examples:webmvc:event-listener",
    "examples:webmvc:schedule",
    "examples:webmvc:condition",
    "examples:webflux:fail-behavior",
    "examples:webflux:error-handling",
    "examples:webflux:custom-provider",
    "examples:webflux:actuator-endpoint",
    "examples:webflux:health-indicator",
    "examples:webflux:basic-usage",
    "examples:webflux:functional-endpoint",
    "examples:webflux:rollout",
    "examples:webflux:custom-rollout-strategy",
    "examples:webflux:schedule",
)
