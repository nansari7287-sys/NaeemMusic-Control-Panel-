plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace = "dev.drakoxnaeem.control"
 compileSdk = 35
 defaultConfig {
  applicationId = "dev.drakoxnaeem.control"
  minSdk = 26
  targetSdk = 35
  versionCode = 2
  versionName = "1.0.0"
  buildConfigField("String", "API_BASE_URL", "\"https://naeem-music-control-api.ansarinaeem5558.workers.dev\"")
 }
 buildFeatures { compose = true; buildConfig = true }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2025.01.00"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
 implementation("com.squareup.okhttp3:okhttp:4.12.0")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
