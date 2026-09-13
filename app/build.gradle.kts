plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    jacoco
}

android {
    namespace = "com.johncorser.telly"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.johncorser.telly"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests.all { test ->
            test.extensions.configure(JacocoTaskExtension::class) {
                isIncludeNoLocationClasses = true
                excludes = listOf("jdk.internal.*")
            }
        }
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    source.setFrom(files("src/main/java", "src/main/kotlin", "src/test/java", "src/test/kotlin"))
}

// Coverage exclusions: only entry points, thin Compose UI, and generated code.
// Logic must live in testable slices (see CLAUDE.md) — fix the code, never the gate.
val coverageExclusions =
    listOf(
        "**/BuildConfig*",
        "**/R.class",
        "**/R$*.class",
        "**/*Activity*",
        "**/*Screen*",
        "**/ComposableSingletons*",
        "**/*_Impl*",
    )

fun jacocoClassDirs() =
    fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") { exclude(coverageExclusions) }

fun jacocoExecData() = files("${layout.buildDirectory.get()}/jacoco/testDebugUnitTest.exec")

val jacocoTestReport by tasks.registering(JacocoReport::class) {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generate JaCoCo coverage report (XML consumed by scripts/crap-check.mjs)."
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(jacocoExecData())
}

val jacocoCoverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    dependsOn("testDebugUnitTest")
    mustRunAfter(jacocoTestReport)
    group = "verification"
    description = "Fail the build when line coverage of main sources drops below 80%."
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(jacocoExecData())
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.ui)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
