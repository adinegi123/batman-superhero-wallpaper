plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.adi121.bat_superhero_man_wallpaper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.adi121.bat_superhero_man_wallpaper"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding =true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.app.update.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    //implementation("com.google.firebase:firebase-analytics-ktx")

    //Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")

    //Glide dependency
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")


    //Paging Library
    implementation("androidx.paging:paging-runtime-ktx:3.1.1")

    // Firebase Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.1.1")

    //sdp
    implementation ("com.intuit.sdp:sdp-android:1.1.0")

    // lottie files
    implementation ("com.airbnb.android:lottie:5.2.0")

    //Swipe to refresh
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Progressbar

   implementation("io.github.maitrungduc1410:AVLoadingIndicatorView:2.1.4")

    //Easy Permissions
    implementation("pub.devrel:easypermissions:3.0.0")

    implementation ("androidx.work:work-runtime-ktx:2.7.0")

    implementation ("io.grpc:grpc-okhttp:1.32.2")

    implementation ("uk.co.samuelwall:material-tap-target-prompt:3.3.2")
}