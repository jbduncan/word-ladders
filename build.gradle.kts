plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("kapt") version "1.9.10"
    application
}

group = "com.github.jbduncan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("com.google.mug:mug:6.6")
    implementation("info.picocli:picocli:4.7.5")
    kapt("info.picocli:picocli-codegen:4.7.5")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.github.jbduncan.MainKt")
}