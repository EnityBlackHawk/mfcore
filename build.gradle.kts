
plugins {
    kotlin("jvm")
    "java-library"
    `maven-publish`
}



group = "org.utfpr.mf"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("org.springframework.data:spring-data-mongodb:4.3.4")
    api("org.mongodb:mongodb-driver-sync:5.1.4")
    implementation("org.springframework:spring-jdbc:6.1.13")
    implementation("dev.langchain4j:langchain4j:1.0.0-beta3")
    implementation("dev.langchain4j:langchain4j-open-ai:1.0.0-beta3")

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
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.26.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.0")

    testImplementation("ch.qos.logback:logback-classic:1.5.12")


}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group as String
            artifactId = "mfcore"
            version = version as String

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