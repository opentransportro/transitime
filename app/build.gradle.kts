plugins {
    id("java")
    id("org.springframework.boot")
    id("com.google.cloud.tools.jib")
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = System.getenv("CONTAINER_REPO")
        auth {
            username = System.getenv("CONTAINER_REGISTRY_USER")
            password = System.getenv("CONTAINER_REGISTRY_PASSWORD")
        }
    }
    container {
        mainClass = "org.transitclock.Application"
        entrypoint = listOf("bash", "-c", "/entrypoint.sh")
        ports = listOf("8080")
        volumes = listOf("/var/transitclock/")
        environment = mapOf(
            "SPRING_OUTPUT_ANSI_ENABLED" to "ALWAYS",
            "JHIPSTER_SLEEP" to "0"
        )
        creationTime = "USE_CURRENT_TIMESTAMP"
        user = "1000"
    }
    extraDirectories {
        permissions = mapOf(
            "/docker-entrypoint.sh" to "755",
            "/waitforit" to "755",
            "/app/config/" to "777",
            "/var/transitclock/" to "755",
        )
    }
}
dependencies {
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    modules {
        module("org.springframework.boot:spring-boot-starter-tomcat") {
            replacedBy("org.springframework.boot:spring-boot-starter-undertow", "Use Undertow instead of Tomcat")
        }
    }
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.3.0")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate6")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-protobuf")

    implementation(project(":libs:core"))
    implementation(project(":libs:util"))

    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("com.google.guava:guava")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

springBoot {
    mainClass = "org.transitclock.Application"
}