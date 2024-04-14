val springVersion: String by project
val lombokVersion: String by project
val queryDslVersion: String by project

plugins {
    id("java")
    id("io.spring.dependency-management")
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")

    dependencies {
        annotationProcessor("org.projectlombok:lombok:${lombokVersion}")
        compileOnly("org.projectlombok:lombok:${lombokVersion}")
    }

    dependencyManagement {
        imports {
            mavenBom("com.querydsl:querydsl-bom:${queryDslVersion}")
            mavenBom("org.springframework.boot:spring-boot-dependencies:${springVersion}")
        }
        dependencies{
            dependency("org.apache.commons:commons-csv:1.10.0")
            dependency("io.hypersistence:hypersistence-utils-hibernate-62:3.7.1")
            dependency("com.google.guava:guava:33.0.0-jre")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
    tasks.register("cleanResources") {
        delete("${layout.buildDirectory}/resources")
    }

    tasks.compileJava {
        dependsOn("cleanResources")
    }


//    tasks.withType<KotlinCompile> {
//        kotlinOptions {
//            freeCompilerArgs += "-Xjsr305=strict"
//            jvmTarget = "21"
//        }
//    }
}
allprojects {
    group = "org.transitclock"
    version = "3.0.0-SNAPSHOT"

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "minutes")
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }
}


tasks.wrapper {
    gradleVersion = "8.3"
}
