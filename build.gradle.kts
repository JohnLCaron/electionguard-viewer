// bug in IntelliJ in which libs shows up as not being accessible
// see https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")

plugins {
    base
    java
    kotlin("jvm") version "1.6.21"
}

group = "electionguard.viewer"
version = "1.0-SNAPSHOT"
val pbandkVersion by extra("0.13.0")

repositories {
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
    implementation(files("libs/electionguard-kotlin-multiplatform-jvm-1.0-SNAPSHOT.jar"))

    implementation(libs.jdom2)
    implementation(libs.slf4j)

    runtimeOnly(libs.slf4jJdk14)
    runtimeOnly(libs.floggerBackend)

    implementation(kotlin("stdlib-common", "1.6.20"))
    implementation(kotlin("stdlib", "1.6.20"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    // Useful, portable routines
    // implementation("io.ktor:ktor-utils:1.6.8")

    // Portable logging interface. On the JVM, we'll get "logback", which gives
    // us lots of features. On Native, it ultimately just prints to stdout.
    // On JS, it uses console.log, console.error, etc.
    implementation("io.github.microutils:kotlin-logging:2.1.21")

    // A multiplatform Kotlin library for working with date and time.
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

    // A multiplatform Kotlin library for working with protobuf.
    implementation("pro.streem.pbandk:pbandk-runtime:$pbandkVersion")

    // A multiplatform Kotlin library for Result monads
    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.15")

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