plugins {
    id("java")
    id("java-library")
    alias(libs.plugins.shadow)
}

// project settings
group = "me.kubbidev"
version = "1.0-SNAPSHOT"

base {
    archivesName.set("laboratory")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    // include source in when publishing
    withSourcesJar()
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")

    api("org.slf4j:slf4j-api:1.7.30")
    api("org.slf4j:slf4j-simple:1.7.30")

    api("com.google.code.gson:gson:2.7")
    api("com.google.guava:guava:19.0")

    // lombok dependencies & annotation processor
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveFileName = "Laboratory-1.0.0.jar"

    dependencies {
        include(dependency("me.kubbidev.laboratory:.*"))
    }
}

artifacts {
    archives(tasks.shadowJar)
}