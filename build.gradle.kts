// bug in IntelliJ in which libs shows up as not being accessible
// see https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")

plugins {
    base
    java
    kotlin("jvm") version "1.7.10"
}

group = "electionguard.viewer"
version = "1.0-SNAPSHOT"
val pbandkVersion by extra("0.13.0")

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/danwallach/electionguard-kotlin-multiplatform")
        credentials {
            username = project.findProperty("github.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("github.key") as String? ?: System.getenv("TOKEN")
        }
    }
    mavenCentral()
    flatDir {
        dirs("libs")
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

dependencies {
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.jsr305)
    implementation(libs.flogger)

    implementation(files("libs/uibase.jar"))
    implementation("electionguard-kotlin-multiplatform:electionguard-kotlin-multiplatform-jvm:1.0-SNAPSHOT")

    implementation(libs.jdom2)
    implementation(libs.slf4j)

    runtimeOnly(libs.slf4jJdk14)
    runtimeOnly(libs.floggerBackend)

    implementation(kotlin("stdlib-common", "1.6.20"))
    implementation(kotlin("stdlib", "1.6.20"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    // A multiplatform Kotlin library for working with date and time.
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

    // A multiplatform Kotlin library for working with protobuf.
    implementation("pro.streem.pbandk:pbandk-runtime:$pbandkVersion")

    // A multiplatform Kotlin library for Result monads
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.15")

    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha12")

    testImplementation(libs.truth)
    testImplementation(libs.truthJava8Extension)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    register("fatJar", Jar::class.java) {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest {
            attributes("Main-Class" to "electionguard.viewer.ViewerMain")
        }
        from(configurations.runtimeClasspath.get()
            // .onEach { println("add from dependencies: ${it.name}") }
            .map { if (it.isDirectory) it else zipTree(it) })
        val sourcesMain = sourceSets.main.get()
        // exclude("/META-INF/PFOPENSO.*")
        // sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
        from(sourcesMain.output)
    }
}