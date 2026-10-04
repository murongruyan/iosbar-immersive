import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application") version "9.4.1"

    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.1"
}

android {
    namespace = "com.iosbar.navhook"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.iosbar.navhook"
        minSdk = 36
        targetSdk = 37
        versionCode = 10
        versionName = "0.6.0"
    }

    sourceSets["main"].apply {
        manifest.srcFile("AndroidManifest.xml")
        java.directories.clear()
        java.directories.add("java")
        kotlin.directories.clear()
        kotlin.directories.add("kotlin")
        res.directories.clear()
        res.directories.add("res")
        resources.directories.clear()
        resources.directories.add("resources")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
            excludes += setOf(
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.version",
                "kotlin/**",
                "kotlin-tooling-metadata.json",
                "DebugProbesKt.bin",
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("org.jetbrains.compose.runtime:runtime:1.12.1")
    implementation("org.jetbrains.compose.foundation:foundation:1.12.1")
    implementation("org.jetbrains.compose.ui:ui:1.12.1")

    implementation("top.yukonga.miuix.kmp:miuix-ui:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-preference:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-icons:0.9.4")
    implementation("top.yukonga.miuix.kmp:miuix-blur:0.9.4")
    implementation("io.github.kyant0:backdrop:2.0.1")
}

tasks.register<Copy>("exportModuleApk") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.file("outputs/apk/release/iosbar-navhook-release.apk"))
    into(rootProject.projectDir.resolve("../../runtime"))
    rename { "iosbar-navhook.apk" }
}

