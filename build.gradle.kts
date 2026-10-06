plugins {
    application
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass = "pr2.Main"
}

tasks.named<JavaExec>("run") {
    workingDir = rootDir
    standardInput = System.`in`
}
