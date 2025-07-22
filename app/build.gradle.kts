import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("kotlin-android")
    id("kotlin-kapt")
    //id("com.google.devtools.ksp") version("1.7.20-1.0.6")
}

android {
    signingConfigs {
        create("release") {
            val properties: Properties = Properties()
            properties.load(project.rootProject.file("release.properties").inputStream())
            storeFile = file(properties.getProperty("storeFile"))
            keyAlias = properties.getProperty("keyAlias")
            storePassword = properties.getProperty("storePassword")
            keyPassword = properties.getProperty("keyPassword")
        }
    }
    namespace = "com.pangaea.idothecooking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pangaea.idothecooking"
        minSdk = 21
        targetSdk = 35
        versionCode = 14
        versionName = "14.0"

        // Load the values from apikey.properties file
        val keystoreFile = project.rootProject.file("apikey.properties")
        val properties = Properties()
        properties.load(keystoreFile.inputStream())
        val apiKey = properties.getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", apiKey)
        ////////////////////////////////////

        testInstrumentationRunner = "androidx.test.ext.junit.runners.AndroidJUnit4"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        applicationVariants.all {
            outputs
                // default type don't have outputFileName field
                .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
                .all { output ->
                    output.outputFileName = "IDoTheCooking.apk"
                    false
                }
        }
    }
    compileOptions {
        //sourceCompatibility = JavaVersion.VERSION_1_8
        //targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        //jvmTarget = "1.8"
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.8.10")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")

    //testImplementation("junit:junit-ktx:4.12")
    testImplementation("junit:junit:4.12")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
//    testImplementation("androidx.test:rules:1.1.2-alpha02")
//    testImplementation("androidx.test:runner:1.1.2-alpha02")
    implementation("androidx.test:core-ktx:1.5.0")
}

dependencies {
    val androidXTestVersion = "1.5.0"
    val testRunnerVersion = "1.5.2"
    val testRulesVersion = "1.5.0"
    val testJunitVersion = "1.1.5"
    val truthVersion = "1.5.0"
    val espressoVersion = "3.5.1"

    androidTestImplementation("androidx.test.espresso:espresso-core:$espressoVersion")

    // Core library
    androidTestImplementation("androidx.test:core:$androidXTestVersion")

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation("androidx.test:runner:$testRunnerVersion")
    androidTestImplementation("androidx.test:rules:$testRulesVersion")

    // Assertions
    androidTestImplementation("androidx.test.ext:junit:$testJunitVersion")
    androidTestImplementation("androidx.test.ext:truth:$truthVersion")

    // Espresso dependencies
    androidTestImplementation( "androidx.test.espresso:espresso-core:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso:espresso-contrib:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso:espresso-intents:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso:espresso-accessibility:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso:espresso-web:$espressoVersion")
    androidTestImplementation( "androidx.test.espresso.idling:idling-concurrent:$espressoVersion")

    // The following Espresso dependency can be either "implementation",
    // or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK"s compile classpath or the test APK
    // classpath.
    androidTestImplementation( "androidx.test.espresso:espresso-idling-resource:$espressoVersion")
}

// Room dependencies
dependencies {
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")//implementation("androidx.legacy:legacy-support-v4:1.0.0")
    //implementation("androidx.recyclerview:recyclerview:1.3.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    val room_version = "2.6.0"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    // For Kotlin use kapt instead of annotationProcessor

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - RxJava support for Room
    //implementation("androidx.room:room-rxjava2:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // Test helpers
    testImplementation("androidx.room:room-testing:2.6.0")
    //implementation 'com.google.android.material:material:1.0.0'
}

dependencies {
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    // Glide v4 uses this new annotation processor -- see https://bumptech.github.io/glide/doc/generatedapi.html
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.8.+")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.robertlevonyan.view:MaterialExpansionPanel:2.1.3")
}

dependencies {
    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}
