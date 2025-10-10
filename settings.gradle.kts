import java.net.URI

rootProject.name = "memoiry"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
}
