plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
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
        versionCode = 2
        versionName = "0.2.0"
        // Gherkin acceptance harness: cucumber-android drives the real app.
        testInstrumentationRunner = "com.johncorser.telly.e2e.TellyCucumberRunner"
        // cucumber-android scans the test-app package for @CucumberOptions
        // by default; the harness lives under .e2e instead.
        testInstrumentationRunnerArguments["optionsAnnotationPackage"] = "com.johncorser.telly.e2e"
    }

    sourceSets {
        // Feature files + binary fixtures live under e2e/ (single source of
        // truth); a Sync task stages them into the androidTest assets.
        getByName("androidTest").assets.srcDir(layout.buildDirectory.dir("generated/e2eAssets"))
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
        // Robolectric (Room DAO/repository tests on the JVM) needs the manifest + resources.
        unitTests.isIncludeAndroidResources = true
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

ksp {
    // Room schema JSON is exported and checked in under app/schemas.
    arg("room.schemaLocation", "$projectDir/schemas")
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
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    // Real XmlPullParser implementation for JVM tests (android.util.Xml is Android-only).
    testImplementation(libs.kxml2)

    // Gherkin acceptance harness (e2e/features executed on an Android TV emulator).
    androidTestImplementation(libs.cucumber.android)
    androidTestImplementation(libs.cucumber.picocontainer)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockwebserver)
}

// Stage e2e feature files + stream/logo fixtures as androidTest assets so the
// suite is hermetic on the emulator (playlist.m3u/epg.xml are generated at
// test runtime with fresh timestamps — see e2e/fixtures/gen-fixtures.mjs).
val syncE2eAssets by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("generated/e2eAssets"))
    from(rootProject.file("e2e/features")) { into("features") }
    from(rootProject.file("e2e/fixtures/streams")) {
        // The tiny vod-sample.mp4 backs the VOD scenarios and the .m3u8
        // playlists back the HLS recording one; the full-length per-channel
        // .mp4 captures stay dev-only (they'd bloat the APK).
        include("*.ts", "*.m3u8", "vod-sample.mp4")
        into("fixtures/streams")
    }
    from(rootProject.file("e2e/fixtures/logos")) { into("fixtures/logos") }
}

tasks.matching { it.name.contains("AndroidTestAssets") }.configureEach { dependsOn(syncE2eAssets) }
