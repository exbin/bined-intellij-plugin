plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "org.exbin.deltahex.intellij"
version = "0.3.0.snapshot"
val ideLocalPath = providers.gradleProperty("ideLocalPath").getOrElse("")

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    type.set("IC") // Target IDE Platform
}

if (ideLocalPath.isEmpty()) {
    intellij {
        version.set("2023.2.1")
        plugins.set(listOf("java"))
    }
} else {
    intellij {
        localPath.set(ideLocalPath)
        // Some variants require to add java plugin due to detection bug
        plugins.set(listOf("java"))
    }
}

sourceSets {
    main {
        resources {
            srcDirs("${rootDir}/src/main/languages")
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "17"
//    }

    patchPluginXml {
        sinceBuild.set("232.1")
        untilBuild.set("")
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

val exbinFrameworkLibraryVersion = "0.3.0-SNAPSHOT"
val binedAppLibraryVersion = "0.3.0-SNAPSHOT"
val binedLibraryVersion = "0.3.0-SNAPSHOT"
val binaryDataLibraryVersion = "0.2.1"

fun exbinFrameworkLibrary(libName: String): String = if (exbinFrameworkLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${exbinFrameworkLibraryVersion}" else "org.exbin.framework:${libName}:${exbinFrameworkLibraryVersion}"
fun binedAppLibrary(libName: String): String = if (binedAppLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${binedAppLibraryVersion}" else "org.exbin.framework:${libName}:${binedAppLibraryVersion}"
fun binedLibrary(libName: String): String = if (libName.endsWith("-SNAPSHOTX")) ":${libName}-${binedLibraryVersion}" else "org.exbin.bined:${libName}:${binedLibraryVersion}"
fun binaryDataLibrary(libName: String): String = if (libName.endsWith("-SNAPSHOT")) ":${libName}-${binaryDataLibraryVersion}" else "org.exbin.auxiliary:${libName}:${binaryDataLibraryVersion}"

repositories {
    flatDir {
        dirs("lib", "lib/jetbrains")
    }
    mavenLocal()
}

dependencies {
    implementation(exbinFrameworkLibrary("exbin-framework"))
    implementation(exbinFrameworkLibrary("exbin-framework-basic"))
    implementation(exbinFrameworkLibrary("exbin-framework-frame"))
    implementation(exbinFrameworkLibrary("exbin-framework-ui"))
    implementation(exbinFrameworkLibrary("exbin-framework-component"))
//    implementation(exbinFrameworkLibrary("exbin-framework-data"))
    implementation(exbinFrameworkLibrary("exbin-framework-window"))
    implementation(exbinFrameworkLibrary("exbin-framework-action"))
    implementation(exbinFrameworkLibrary("exbin-framework-file"))
    implementation(exbinFrameworkLibrary("exbin-framework-editor"))
    implementation(exbinFrameworkLibrary("exbin-framework-about-api"))
    implementation(exbinFrameworkLibrary("exbin-framework-about"))
    implementation(exbinFrameworkLibrary("exbin-framework-operation"))
    implementation(exbinFrameworkLibrary("exbin-framework-operation-undo"))
    implementation(exbinFrameworkLibrary("exbin-framework-action-popup"))
    implementation(exbinFrameworkLibrary("exbin-framework-help-online"))
    implementation(exbinFrameworkLibrary("exbin-framework-options"))
    implementation(exbinFrameworkLibrary("exbin-framework-options-api"))
    implementation(exbinFrameworkLibrary("exbin-framework-preferences-api"))
    implementation(exbinFrameworkLibrary("exbin-framework-preferences"))
    implementation(exbinFrameworkLibrary("exbin-framework-language-api"))
    implementation(exbinFrameworkLibrary("exbin-framework-language"))
    implementation(exbinFrameworkLibrary("exbin-framework-editor-text"))
    implementation(binedAppLibrary("exbin-framework-bined"))
    implementation(binedAppLibrary("exbin-framework-bined-bookmarks"))
    implementation(binedAppLibrary("exbin-framework-bined-compare"))
    implementation(binedAppLibrary("exbin-framework-bined-inspector"))
    implementation(binedAppLibrary("exbin-framework-bined-macro"))
    implementation(binedAppLibrary("exbin-framework-bined-objectdata"))
    implementation(binedAppLibrary("exbin-framework-bined-operation"))
    implementation(binedAppLibrary("exbin-framework-bined-operation-bouncycastle"))
    implementation(binedAppLibrary("exbin-framework-bined-search"))
    implementation(binedAppLibrary("exbin-framework-bined-tool-content"))
    implementation(binedLibrary("bined-core"))
    implementation(binedLibrary("bined-extended"))
    implementation(binedLibrary("bined-highlight-swing"))
    implementation(binedLibrary("bined-operation"))
    implementation(binedLibrary("bined-operation-swing"))
    implementation(binedLibrary("bined-swing"))
    implementation(binedLibrary("bined-swing-extended"))
    implementation(binaryDataLibrary("binary_data"))
    implementation(binaryDataLibrary("binary_data-paged"))
    implementation(binaryDataLibrary("binary_data-delta"))
    implementation(":flatlaf-desktop-3.2")
    compileOnly(":debugvalue-clion-2022.2.1")
    compileOnly(":debugvalue-goland-2022.2.1")
    compileOnly(":debugvalue-intellij-2022.2.1")
    compileOnly(":debugvalue-phpstorm-2022.2.1")
    compileOnly(":debugvalue-pycharm-2022.2.1")
    compileOnly(":debugvalue-rider-2022.2.1")
    compileOnly(":database-plugin-2022.2.1")
    compileOnly(":jsr305-2.0.1")
}
