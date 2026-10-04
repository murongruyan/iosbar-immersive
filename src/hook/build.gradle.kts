import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Base64

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
        versionCode = 11
        versionName = "0.6.1"
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

    // 发布签名：沿用慕容调度那套 keystore（别名 慕容调度），两个模块的更新因此可以互相覆盖安装。
    // CI 从 KEYSTORE_BASE64（GitHub Secrets）解码出 release.jks，本地直接用 src/hook/release.jks
    // （已被 .gitignore 排除）。缺少签名材料时构建会直接失败，而不是悄悄产出未签名的包。
    signingConfigs {
        create("release") {
            val ksFile = file("release.jks")
            val encoded = (findProperty("KEYSTORE_BASE64") as String?) ?: System.getenv("KEYSTORE_BASE64")
            if (!encoded.isNullOrBlank()) {
                val clean = encoded.replace(
                    Regex("-----BEGIN CERTIFICATE-----|-----END CERTIFICATE-----|[\\r\\n]"),
                    "",
                )
                ksFile.parentFile?.mkdirs()
                ksFile.writeBytes(Base64.getDecoder().decode(clean))
            }
            storeFile = ksFile
            storePassword = (findProperty("STORE_PASSWORD") as String?) ?: System.getenv("STORE_PASSWORD") ?: ""
            keyAlias = "慕容调度"
            keyPassword = (findProperty("KEY_PASSWORD") as String?) ?: System.getenv("KEY_PASSWORD") ?: ""
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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

