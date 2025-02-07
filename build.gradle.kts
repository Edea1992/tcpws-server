plugins {
    id("java")
    id("org.beryx.jlink") version "3.1.+"
    id("com.bmuschko.docker-java-application") version "9.4.+"
}

group = "com.nbintelligence"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(platform("io.netty:netty-bom:4.1.117.Final"))
    implementation("io.netty:netty-codec-http")
    implementation("io.netty:netty-transport-native-epoll")
    implementation("io.netty:netty-transport-native-kqueue")

    runtimeOnly("io.netty:netty-transport-native-epoll:4.1.117.Final:linux-x86_64")
    runtimeOnly("io.netty:netty-transport-native-epoll:4.1.117.Final:linux-aarch_64")
    runtimeOnly("io.netty:netty-transport-native-kqueue:4.1.117.Final:osx-x86_64")
    runtimeOnly("io.netty:netty-transport-native-kqueue:4.1.117.Final:osx-aarch_64")
    runtimeOnly("io.projectreactor.tools:blockhound:1.0.10.RELEASE")
}

application {
    mainModule = "com.nbintelligence.ws2tcp"
    mainClass = "com.nbintelligence.ws2tcp.Main"
}

jlink {
    options = listOf(
        "--no-header-files",
    )
}

tasks.test {
    useJUnitPlatform()
}