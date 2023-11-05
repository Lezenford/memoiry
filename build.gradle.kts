import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.lezenford.telegram"
version = "0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    implementation("org.telegram:telegrambots:6.8.0") {
        exclude("commons-logging", "commons-logging")
    }
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.100.Final:osx-aarch_64")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
}

tasks.register("dockerfile") {
    dependsOn("bootJar")
    doLast {
        project.exec {
            commandLine(
                "docker", "build",
                "-t", "cr.yandex/crp6rh6dggdnd2kedq1q/memoiry:$version",
                "--platform=linux/amd64",
                "."
            )
        }
    }
}

tasks.register("pushImage") {
    dependsOn("dockerfile")
    doLast {
        project.exec {
            commandLine("docker", "push", "cr.yandex/crp6rh6dggdnd2kedq1q/memoiry:$version")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
