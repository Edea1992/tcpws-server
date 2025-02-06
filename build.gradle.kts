plugins {
    id("java")
    id("org.beryx.jlink") version "3.1.1"
    id("com.bmuschko.docker-java-application") version "9.4.0"
}

group = "com.nbintelligence"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.projectreactor.tools:blockhound:1.0.10.RELEASE")

    implementation("io.netty:netty-transport-native-epoll:4.1.117.Final:linux-x86_64")
    implementation("io.netty:netty-transport-native-epoll:4.1.117.Final:linux-aarch_64")
    implementation("io.netty:netty-transport-native-kqueue:4.1.117.Final:osx-x86_64")
    implementation("io.netty:netty-transport-native-kqueue:4.1.117.Final:osx-aarch_64")
    implementation("io.netty:netty-codec-http:4.1.117.Final")
}

application {
    mainModule = "com.nbintelligence.ws2tcp"
    mainClass = "com.nbintelligence.ws2tcp.Main"
}

jlink {
    options = listOf(
        "--no-header-files",
    )

    launcher {
        jvmArgs = listOf(
            "-Xms128m",
        )
    }
}

tasks.test {
    useJUnitPlatform()
}