import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.lezenford.telegram.memoiry"
version = "2.1"

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

ktor {
    docker {
        jreVersion = JavaVersion.VERSION_25
        localImageName = "cr.yandex/crp7ivvonmm5cmci2uht/memoiry"
        imageTag = "$version"
    }
}

jib {
    dockerClient {
        this.executable = "/opt/homebrew/bin/docker"
    }

    container {
        jvmFlags = listOf(
            "--enable-native-access=ALL-UNNAMED",
            "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
            "--add-opens", "java.base/java.nio=ALL-UNNAMED",
            "-Dio.netty.tryReflectionSetAccessible=true"
        )
    }
}

application {
    mainClass = "com.lezenford.telegram.ApplicationKt"
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true"
    )
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    implementation(platform(libs.ydb.bom))
    implementation(libs.ydb.sdk.auth)
    implementation(libs.ydb.sdk.table)
    implementation(libs.telegram.bot.api)
    implementation(libs.retrofit)
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.compilerOptions {
    freeCompilerArgs.set(listOf("-Xcontext-parameters"))
}

tasks.register<Exec>("pushDockerImage") {
    dependsOn("publishImageToLocalRegistry")

    val imageName = "cr.yandex/crp7ivvonmm5cmci2uht/memoiry"
    val imageTag = version.toString()
    val fullImageName = "$imageName:$imageTag"

    commandLine("/opt/homebrew/bin/docker", "push", fullImageName)
}
