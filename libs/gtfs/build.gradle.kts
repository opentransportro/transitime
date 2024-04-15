plugins {
    id("java-library")
    id("com.google.protobuf")
}

dependencies {
    implementation(project(":libs:util"))
    implementation("org.apache.commons:commons-lang3")
    implementation("org.apache.commons:commons-csv")
    implementation("com.google.guava:guava")

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