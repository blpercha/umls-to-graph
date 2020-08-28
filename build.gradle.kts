plugins {
    kotlin("jvm") version "1.4.0"
    id("org.jmailen.kotlinter") version "3.0.2"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

repositories {
    jcenter()
}

dependencies {
    implementation("net.sf.trove4j", "trove4j", "3.0.3")
    implementation("org.apache.commons", "commons-lang3", "3.3.2")
    implementation("com.google.guava", "guava", "19.0")

    implementation("com.github.ajalt.clikt", "clikt", "3.0.0-rc")

    testImplementation("junit", "junit", "4.12")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "com.foo.unmls.UnmlsGraphTool"
}

tasks {
    test {
        useJUnit()
    }

    // save time by skipping outputs we don't care about
    listOf(distZip, distTar, shadowDistZip, shadowDistTar).forEach { taskProvider ->
        taskProvider {
            enabled = false
        }
    }
}
