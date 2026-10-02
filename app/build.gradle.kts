import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

// ---- 签名凭据加载 ----------------------------------------------------
// 优先级：keystore.properties（本地，已 gitignore）→ 环境变量（CI secrets）。
// 在 android {} 之外读取，避免 signingConfig lambda 的 receiver 遮蔽 java.* 等名字。
val signingProps = rootProject.file("keystore.properties")
    .takeIf { it.isFile }
    ?.inputStream()
    ?.use { stream -> Properties().apply { load(stream) } }

fun signingCredential(property: String, env: String): String? =
    signingProps?.getProperty(property)?.takeIf { it.isNotBlank() }
        ?: System.getenv(env)?.takeIf { it.isNotBlank() }

val signingCredentials = mapOf(
    "storeFile" to signingCredential("storeFile", "KEYSTORE_FILE"),
    "storePassword" to signingCredential("storePassword", "KEY_STORE_PASSWORD"),
    "keyAlias" to signingCredential("keyAlias", "KEY_ALIAS"),
    "keyPassword" to signingCredential("keyPassword", "KEY_PASSWORD"),
)

// assembleRelease 若拿不到完整凭据就立即失败，而不是静默产出未签名 APK
// （此前 signingConfig 悄悄为 null，构建显示成功，装上去被系统拒绝）。
// 只拦 release，assembleDebug 不受影响。
val missingSigningCredentials = signingCredentials.filterValues { it == null }.keys
if (missingSigningCredentials.isNotEmpty()) {
    val releaseRequested = gradle.startParameter.taskNames.any { task ->
        task.contains("Release", ignoreCase = true)
    }
    if (releaseRequested) {
        throw GradleException(
            """
            |Animius: 缺少签名凭据，无法打出可安装的 release APK。
            |
            |缺失项: ${missingSigningCredentials.joinToString(", ")}
            |
            |解决方式（二选一）：
            |  1. 在仓库根目录创建 keystore.properties（已 gitignore），内容:
            |       storeFile=keystore.jks
            |       storePassword=<密码>
            |       keyAlias=<别名>
            |       keyPassword=<密码>
            |  2. 设置环境变量: KEYSTORE_FILE / KEY_STORE_PASSWORD / KEY_ALIAS / KEY_PASSWORD
            |
            |CI 上对应 GitHub Secrets: SIGNING_KEY_BASE64, KEY_STORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
            |
            |注意: 不要回退到 debug 签名 —— 日后换成正式签名时，用户必须先卸载旧包才能升级。
            """.trimMargin()
        )
    }
}

android {
    namespace = "com.lanlinju.animius"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lanlinju.animius"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = compileSdk
        versionCode = 37
        // 发布 tag 必须与此一致：应用内更新检查是字符串比较 MainViewModel 里
        // updateVersionName != "v${BuildConfig.VERSION_NAME}"，不做 semver 解析。
        versionName = "1.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val dandanplayAppId = System.getenv("DANDANPLAY_APP_ID") ?: ""
        val dandanplayAppSecret = System.getenv("DANDANPLAY_APP_SECRET") ?: ""
        buildConfigField("String", "DANDANPLAY_APP_ID", "\"$dandanplayAppId\"")
        buildConfigField("String", "DANDANPLAY_APP_SECRET", "\"$dandanplayAppSecret\"")
    }

    signingConfigs {
        create("release") {
            val store = signingCredentials.getValue("storeFile")
            if (store != null) {
                storeFile = rootProject.file(store)
                storePassword = signingCredentials.getValue("storePassword")
                keyAlias = signingCredentials.getValue("keyAlias")
                keyPassword = signingCredentials.getValue("keyPassword")
            }
            // 凭据不完整时保持空配置：上面的校验会让 assembleRelease 先失败，
            // 而 assembleDebug 根本不碰这个 signingConfig。
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${rootProject.name}-v${defaultConfig.versionName}-$name.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":video-player"))
    implementation(project(":download"))
    implementation(project(":danmaku"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation.layout.android)

    // icons
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.material)

    // navigation component
    implementation(libs.androidx.navigation.compose)

    // jsoup
    implementation(libs.jsoup)

    // hilt
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

    // hilt navigation
    implementation(libs.androidx.hilt.navigation.compose)

    // coil
    implementation(libs.coil.compose)

    // room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // paging compose
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.compose.android)

    // splash screen
    implementation(libs.androidx.splashscreen)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.client.mock)
    implementation(libs.ktor.serialization.kotlinx.json)

    // serialization
    implementation(libs.kotlinx.serialization.json)

    // Slf4j
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
