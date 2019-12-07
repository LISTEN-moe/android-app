val ktlint: Configuration by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.36.0")
}

tasks {
    register<JavaExec>("ktlint") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style"
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("src/**/*.kt")
    }

    register<JavaExec>("ktlintFormat") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Fix Kotlin code style deviations"
        classpath = ktlint
        main = "com.pinterest.ktlint.Main"
        args("-F", "src/**/*.kt")
    }
}
