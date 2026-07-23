plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Room exports its schema JSON here on each build (see `room.schemaLocation`
// below). Commit these files: they are the history MigrationTestHelper needs
// to test future migrations.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.havistudio.android.greekconstitution"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.havistudio.android.greekconstitution"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel6Api30") {
                    device = "Pixel 6"
                    apiLevel = 30
                    systemImageSource = "aosp-atd"
                    require64Bit = true
                }
            }
        }
    }
}

// Coverage threshold gates. Each parses a JaCoCo XML report and fails the
// build when total LINE coverage drops below the configured floor.
//
// `verifyCoverageThreshold`         — unit tests only (`createDebugUnitTestCoverageReport`).
// `verifyAndroidTestCoverageThreshold` — instrumented tests (`createDebugAndroidTestCoverageReport`).
//
// Floors act as a ratchet: raise them as suites grow.
val unitCoverageLineFloorPercent = 16
val androidTestCoverageLineFloorPercent = 59

fun Project.registerCoverageGate(
    taskName: String,
    reportProvider: () -> java.io.File,
    floorPercent: Int,
    dependsOnTask: String,
) {
    tasks.register(taskName) {
        group = "verification"
        description = "Fails if total LINE coverage is below $floorPercent%."
        dependsOn(dependsOnTask)

        doLast {
            val report = reportProvider()
            if (!report.exists()) {
                throw GradleException("Coverage report not found at ${report.path}")
            }

            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply {
                setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                isValidating = false
            }
            val doc = factory.newDocumentBuilder().parse(report)
            val rootCounters = doc.documentElement.childNodes
            var missed = 0L
            var covered = 0L
            for (i in 0 until rootCounters.length) {
                val n = rootCounters.item(i)
                if (n.nodeName == "counter" && n.attributes.getNamedItem("type")?.nodeValue == "LINE") {
                    missed = n.attributes.getNamedItem("missed").nodeValue.toLong()
                    covered = n.attributes.getNamedItem("covered").nodeValue.toLong()
                    break
                }
            }
            val total = missed + covered
            val pct = if (total == 0L) 0.0 else covered.toDouble() * 100.0 / total
            val pctRounded = "%.2f".format(pct)
            if (pct < floorPercent) {
                throw GradleException(
                    "[$taskName] Line coverage $pctRounded% is below floor $floorPercent% " +
                        "(covered=$covered, missed=$missed)."
                )
            }
            println("[$taskName] Line coverage $pctRounded% (covered=$covered, missed=$missed) >= floor $floorPercent%.")
        }
    }
}

registerCoverageGate(
    taskName = "verifyCoverageThreshold",
    reportProvider = {
        layout.buildDirectory.file("reports/coverage/test/debug/report.xml").get().asFile
    },
    floorPercent = unitCoverageLineFloorPercent,
    dependsOnTask = "createDebugUnitTestCoverageReport",
)

registerCoverageGate(
    taskName = "verifyAndroidTestCoverageThreshold",
    reportProvider = {
        layout.buildDirectory
            .file("reports/coverage/androidTest/debug/connected/report.xml")
            .get().asFile
    },
    floorPercent = androidTestCoverageLineFloorPercent,
    dependsOnTask = "createDebugAndroidTestCoverageReport",
)

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Browser (Custom Tabs for source/privacy links)
    implementation(libs.androidx.browser)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(kotlin("reflect"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
