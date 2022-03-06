plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.cookie"
version = ""

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.citizensnpcs.co/") }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(files("G:\\coding\\Test Servers\\TimeRewards\\plugins\\CookieCore.jar"))
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.29-SNAPSHOT")
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks{
    shadowJar{
        archiveClassifier.set("")
        project.configurations.implementation.get().isCanBeResolved = true
        configurations = listOf(project.configurations.implementation.get())
        destinationDirectory.set(file("G:\\coding\\Test Servers\\TimeRewards\\plugins"))
    }
}