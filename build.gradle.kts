plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.cookie"
version = ""

repositories {
    mavenCentral()
    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        url = uri("https://ci.ender.zone/plugin/repository/everything/")
    }
    maven {
        url = uri("https://repo.glaremasters.me/repository/public/")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(files("D:\\coding\\Test Servers\\TimeRewards\\plugins\\CookieCore.jar"))
    compileOnly("com.griefcraft.lwc:LWCX:2.2.6")
    compileOnly("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT")
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
        destinationDirectory.set(file("D:\\coding\\Test Servers\\TimeRewards\\plugins"))
    }
}