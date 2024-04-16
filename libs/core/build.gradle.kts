plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation(project(":libs:extensions:api"))
    implementation(project(":libs:util"))
    runtimeOnly("org.postgresql:postgresql")
    api("com.beust:jcommander:1.82")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    api("org.hibernate.orm:hibernate-core")
    implementation("com.zaxxer:HikariCP")
    implementation("org.hibernate.orm:hibernate-hikaricp")
    implementation("org.hibernate.orm:hibernate-jcache")
    implementation("org.hibernate.validator:hibernate-validator")
    annotationProcessor("org.hibernate.orm:hibernate-jpamodelgen")

    implementation("org.flywaydb:flyway-core")
    implementation("io.hypersistence:hypersistence-utils-hibernate-62")
    api(group="com.querydsl", name="querydsl-jpa", classifier = "jakarta")
    annotationProcessor(group="com.querydsl", name="querydsl-apt", classifier = "jakarta") {
        dependencies {
            compileOnly("jakarta.persistence:jakarta.persistence-api")
        }
    }

    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-csv")

    implementation("com.google.guava:guava")

    api("org.json:json:20231013")
    implementation("org.jasypt:jasypt:1.9.3")

    api(group = "org.ehcache", name = "ehcache", classifier = "jakarta") {
        exclude("com.sun.xml.fastinfoset", "*")
        exclude("javax.xml.bind", "jaxb-api")
        exclude("com.sun.istack", "istack-commons-runtime")
    }

    implementation("com.esotericsoftware:kryo:4.0.0")
    implementation("com.github.haifengl:smile-core:1.5.1")
    implementation("org.glassfish.jaxb:jaxb-core")
    implementation("org.apache.httpcomponents:httpcore")
    implementation("commons-codec:commons-codec")

    protobuf(files("src/proto"))
    api("com.google.protobuf:protobuf-java:4.26.1")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }
}