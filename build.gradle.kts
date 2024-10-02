
plugins {
    kotlin("jvm")
    "java-library"
    `maven-publish`
}



group = "org.utfpr.mf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.data:spring-data-mongodb:4.3.4")
    implementation("org.mongodb:mongodb-driver-sync:5.1.4")
    implementation("org.springframework:spring-jdbc:6.1.13")
    implementation("dev.langchain4j:langchain4j:0.34.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.34.0")

    // Dev tools
    testCompileOnly ("org.projectlombok:lombok:1.18.34")
    testAnnotationProcessor ("org.projectlombok:lombok:1.18.34")
    compileOnly ("org.projectlombok:lombok:1.18.34")
    annotationProcessor ("org.projectlombok:lombok:1.18.34")
    api(kotlin("stdlib-jdk8"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.querydsl:querydsl-apt:5.0.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("commons-codec:commons-codec:1.17.1")

}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "mfcore"
            version = "1.1-SNAPSHOT"

            from(components["java"])
        }
    }
}



tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}