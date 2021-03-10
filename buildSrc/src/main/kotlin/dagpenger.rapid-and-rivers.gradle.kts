plugins {
    id("dagpenger.common")
    application
}

dependencies {
    implementation(RapidAndRivers)

    implementation(Konfig.konfig)
    implementation(Kotlin.Logging.kotlinLogging)
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass.get()))
    }

    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    )
}
