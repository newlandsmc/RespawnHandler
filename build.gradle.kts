plugins {
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.cookie"
version = ""

repositories {
    mavenCentral()
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://repo.citizensnpcs.co/") }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.7.0")
    compileOnly(files("./lib/CookieCore.jar"))
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
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
        //destinationDirectory.set(file("G:\\coding\\Test Servers\\TimeRewards\\plugins"))
    }
    register("copyPlugin", Copy::class.java) {
        from("build/libs/RespawnHandler.jar")
        into("run/plugins")
        dependsOn(shadowJar)
    }
}
