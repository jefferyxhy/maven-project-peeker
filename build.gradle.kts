plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.jefferyxhy.plugins"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
  version.set("2023.2.6")
  type.set("IC") // Target IDE Platform

  plugins.set(listOf(
    "maven"
  ))
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("242.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

dependencies {
  // 3rd party dependencies
  implementation("com.google.code.gson:gson:2.10.1")
  implementation("org.openjdk.nashorn:nashorn-core:15.4")
  implementation("org.apache.maven:maven-model:3.5.0")
  implementation("org.apache.maven:maven-core:3.9.8")
  implementation("org.apache.maven:maven-core:3.9.8")
  implementation("org.apache.maven.shared:maven-invoker:3.2.0")
  implementation("org.ehcache:ehcache:3.10.8")
  implementation("org.slf4j:log4j-over-slf4j:2.0.16")
}
