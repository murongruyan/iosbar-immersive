plugins {
    id("com.android.application") version "9.2.1"
}

android {
    namespace = "com.iosbar.navhook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iosbar.navhook"
        minSdk = 36
        targetSdk = 36
        versionCode = 8
        versionName = "0.4.1"
    }

    sourceSets["main"].apply {
        manifest.srcFile("AndroidManifest.xml")
        java.directories.clear()
        java.directories.add("java")
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

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources { merges += "META-INF/xposed/*" }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
}

tasks.register<Copy>("exportModuleApk") {
    dependsOn("assembleRelease")
    from(layout.buildDirectory.file("outputs/apk/release/iosbar-navhook-release.apk"))
    into(rootProject.projectDir.resolve("../../runtime"))
    rename { "iosbar-navhook.apk" }
}
