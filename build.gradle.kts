plugins {
    kotlin("jvm") version "1.4.0"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "edu.erittenhouse"
version = "1.1.1"

repositories {
    mavenCentral()
}

application {
    mainClassName = "edu.erittenhouse.gitlabtimetracker.MainKt"
}

dependencies {
    val coroutinesVersion = "1.3.6"
    val jacksonVersion = "2.10.0"
    val ktorVersion = "1.3.0"

    // Application dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$coroutinesVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("joda-time:joda-time:2.10.5")

    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.mockk:mockk:1.10.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}