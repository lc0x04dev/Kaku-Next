plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.kakunext.kmyrsv"
    minSdk = 24
    targetSdk = 36
    versionCode = 257
    versionName = "2.7.8-dev.1-test"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

abstract class CopyApkForDownloadTask : DefaultTask() {
  @get:Internal
  abstract val rootDirectory: DirectoryProperty

  @get:Internal
  abstract val buildDirectory: DirectoryProperty

  @TaskAction
  fun run() {
    try {
      val root = rootDirectory.get().asFile
      val bld = buildDirectory.get().asFile
      val possibleSources = listOf(
        File("/.build-outputs/app-debug.apk"),
        File(root, ".build-outputs/app-debug.apk"),
        File(bld, "outputs/apk/debug/app-debug.apk"),
        File(bld, "outputs/apk/debug/app/debug/app-debug.apk")
      )
      
      var copied = false
      for (source in possibleSources) {
        if (source.exists()) {
          val destTest = File(root, "app-debug.test")
          val destNoExt = File(root, "app-debug")
          source.copyTo(destTest, overwrite = true)
          source.copyTo(destNoExt, overwrite = true)
          
          val bldDestTest = File(bld, "app-debug.test")
          val bldDestNoExt = File(bld, "app-debug")
          source.copyTo(bldDestTest, overwrite = true)
          source.copyTo(bldDestNoExt, overwrite = true)
          
          println("==================================================")
          println("🎉 APK COPIADO EXITOSAMENTE PARA DESCARGAR 🎉")
          println("Destino 1: ${destTest.absolutePath}")
          println("Destino 2: ${destNoExt.absolutePath}")
          println("Destino 3: ${bldDestTest.absolutePath}")
          println("==================================================")
          copied = true
          break
        }
      }
      if (!copied) {
        println("=== ADVERTENCIA: No se encontró el APK de origen para copiar ===")
      }
    } catch (e: Exception) {
      println("=== Error en copiar APK: ${e.message} ===")
    }
  }
}

val copyApkTask = tasks.register<CopyApkForDownloadTask>("copyApkForDownload") {
  rootDirectory.set(rootDir)
  buildDirectory.set(layout.buildDirectory)
}

tasks.configureEach {
  if (name.contains("assembleDebug", ignoreCase = true) || name.contains("packageDebug", ignoreCase = true)) {
    finalizedBy(copyApkTask)
  }
}

