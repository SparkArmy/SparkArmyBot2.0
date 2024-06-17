pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.0"
    }
}


rootProject.name = "SparkArmyBot"

var springVersion: String = "3.3.0"
var springGroupId: String = "org.springframework.boot"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("jda", "net.dv8tion", "JDA").version("5.0.0-beta.24")
            library("webhooks", "club.minnced", "discord-webhooks").version("0.8.4")
            library("json", "org.json", "json").version("20240303")
            library("postgres", "org.postgresql", "postgresql").version("42.7.3")
            library("twitch-helix", "com.github.com.twitch4j", "twitch4j-helix").version("1.20.0")
            library("twitch-util", "com.github.com.twitch4j", "twitch4j-util").version("1.20.0")
            library(
                "twitch4j-reactor",
                "com.github.philippheuer.events4j",
                "events4j-handler-reactor"
            ).version("0.12.1")
            library("youtube", "com.google.apis", "google-api-services-youtube").version("v3-rev20240514-2.0.0")
            library("logback", "ch.qos.logback", "logback-classic").version("1.5.6")
            library("slf4j", "org.slf4j", "slf4j-api").version("2.0.13")
            library("hikari", "com.zaxxer", "HikariCP").version("5.1.0")
            library("hibernate-core", "org.hibernate.orm", "hibernate-core").version("6.5.2.Final")
            library("hibernate-hikari", "org.hibernate.orm", "hibernate-hikaricp").version("6.5.2.Final")
            library("flyway", "org.flywaydb", "flyway-database-postgresql").version("10.15.0")
            library("spring-web", "org.springframework", "spring-web").version("6.1.8")
            library("spring-boot", springGroupId, "spring-boot").version(springVersion)
            library("spring-boot-autoconfigure", springGroupId, "spring-boot-autoconfigure").version(springVersion)
            library("tomcat-starter", springGroupId, "spring-boot-starter-tomcat").version(springVersion)
            library("spring-actuator", springGroupId, "spring-boot-actuator").version(springVersion)
            library("spring-data-rest", springGroupId, "spring-boot-starter-data-rest").version(springVersion)
            library("okhttp", "com.squareup.okhttp3", "okhttp").version("4.12.0")
            library("jetbrains-annotations", "org.jetbrains", "annotations").version("24.1.0")
            library("kotlin", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").version("2.0.0")
        }
    }
}



