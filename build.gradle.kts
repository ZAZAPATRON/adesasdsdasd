plugins {
    java
    id("fabric-loom") version "1.3.10"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.8")
    mappings("net.fabricmc:yarn:1.21.8+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.15.7")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.85.0+1.21.8")
}

loom {
    accessWidenerPath.set(file("src/main/resources/fabric.mod.json"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Note: Versions above are indicative. Run Gradle in your environment to sync and build.