/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.lineageos.generatebp.GenerateBpPluginExtension
import org.lineageos.generatebp.models.Module

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.lineageos.generatebp)
}

android {
    namespace = "org.lineageos.aperture"
    compileSdk = 36

    signingConfigs {
        create("blackberry") {
            storeFile = file("keystore/blackberry_aperture.jks")
            storePassword = "blackberry"
            keyAlias = "blackberry_aperture"
            keyPassword = "blackberry"
        }
    }

    defaultConfig {
        applicationId = "org.lineageos.aperture.blackberry"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.4.1"
    }

    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization.
            isMinifyEnabled = true

            // Enables resource shrinking.
            isShrinkResources = true

            signingConfig = signingConfigs.getByName("blackberry")

            // Includes the default ProGuard rules files.
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }

        debug {
            // High-performance non-debuggable build signed with dedicated blackberry keystore
            isDebuggable = false
            signingConfig = signingConfigs.getByName("blackberry")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    lint {
        lintConfig = file("lint.xml")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.viewfinder.core)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
    implementation(libs.coil)
    implementation(libs.coil.video)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.zxing.core)
    implementation(libs.zxing.cpp.android)
}

configure<GenerateBpPluginExtension> {
    targetSdk.set(android.defaultConfig.targetSdk!!)
    minSdk.set(android.defaultConfig.minSdk!!)
    versionCode.set(android.defaultConfig.versionCode!!)
    versionName.set(android.defaultConfig.versionName!!)
    availableInAOSP.set { module: Module ->
        when {
            module.group == "androidx.core" -> {
                // We provide our own core-backported-fixes
                module.name != "core-backported-fixes"
            }

            module.group.startsWith("androidx") -> {
                // We provide our own androidx.{camera,media3}
                !module.group.startsWith("androidx.camera") &&
                        !module.group.startsWith("androidx.media3")
            }

            module.group.startsWith("org.jetbrains") -> {
                // We provide our own atomicfu
                !module.name.startsWith("atomicfu")
            }

            module.group == "com.google.android.material" -> true
            module.group == "com.google.auto.value" -> true
            module.group == "com.google.guava" -> true
            module.group == "javax.inject" -> true
            else -> false
        }
    }
}
