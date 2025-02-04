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
    implementation("io.netty:netty-codec-http:4.1.117.Final")
}

application {
    mainModule = "com.nbintelligence.tcpws.server"
    mainModule = "com.nbintelligence.tcpws.server.Main"
}

jlink {
    launcher {
        jvmArgs = listOf(
            "-Xms4m"
        )
    }
}

tasks.test {
    useJUnitPlatform()
}