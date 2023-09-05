// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenLocal()
    }
}

plugins {
    id("com.android.application") version "7.2.0" apply false
    id("com.android.library") version "7.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
}

ext {
    set("roomVersion", "2.4.3")
    set("nav_version", "2.5.2")
}

tasks.register<Delete>("clean") {
    rootProject.buildDir.delete()
}