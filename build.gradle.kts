plugins {
    kotlin("jvm") version "1.4.0"
    application
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "edu.erittenhouse"
version = "1.4.0"

repositories {
    mavenCentral()
    jcenter()
}

application {
    mainClassName = "edu.erittenhouse.gitlabtimetracker.MainKt"
}

dependencies {
    val coroutinesVersion = "1.4.1"
    val jacksonVersion = "2.10.0"
    val ktorVersion = "1.4.1"
    val slackAPIversion = "1.2.1"

    // Application dependencies
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.controlsfx:controlsfx:8.40.18")
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:$coroutinesVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("joda-time:joda-time:2.10.5")
    implementation("com.slack.api:slack-api-client:$slackAPIversion")
    implementation("com.slack.api:slack-api-model-kotlin-extension:$slackAPIversion")
    implementation("com.slack.api:slack-api-client-kotlin-extension:$slackAPIversion")

    // Testing dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
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
