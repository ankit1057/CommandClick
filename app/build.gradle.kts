plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

val applicationName = "CommandClick"
val versionMajor = 0
val versionMinor = 0
val versionPatch = 16

android {
    namespace = "com.puutaro.commandclick"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.puutaro.commandclick"
        minSdk = 27
        targetSdk = 33
        versionCode = versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        base.archivesName.set("${applicationName}-${versionName}")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    dataBinding {
        enable = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.test:core-ktx:1.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.fragment:fragment-ktx:1.5.6")
    implementation("com.termux.termux-app:termux-shared:0.117")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.github.Yumenokanata:KeyboardVisibilityEvent:1.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.1")
    implementation("android.arch.lifecycle:extensions:1.1.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("commons-io:commons-io:2.11.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.github.omadahealth:swipy:1.2.3@aar")
    implementation("com.github.skydoves:colorpickerview:2.2.4")
    implementation("com.anggrayudi:storage:1.5.4")
    implementation("com.github.sya-ri:kgit:1.0.5")

}