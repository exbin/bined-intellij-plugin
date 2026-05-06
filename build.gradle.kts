plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "org.exbin.deltahex.intellij"
version = "0.2.14-SNAPSHOT"
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
        options.compilerArgs = options.compilerArgs + "-Xlint:unchecked" + "-Xlint:deprecation"
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

val jaguifLibraryVersion = "0.3.0-SNAPSHOT"
val binedAppLibraryVersion = "0.3.0-SNAPSHOT"
val binedLibraryVersion = "0.3.0-SNAPSHOT"
val binaryDataLibraryVersion = "0.3.0-SNAPSHOT"

fun jaguifLibrary(libName: String): String = if (jaguifLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${jaguifLibraryVersion}" else "org.exbin.jaguif:${libName}:${jaguifLibraryVersion}"
fun binedAppLibrary(libName: String): String = if (binedAppLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${binedAppLibraryVersion}" else "org.exbin.bined.jaguif:${libName}:${binedAppLibraryVersion}"
fun binedLibrary(libName: String): String = if (binedLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${binedLibraryVersion}" else "org.exbin.bined:${libName}:${binedLibraryVersion}"
fun binaryDataLibrary(libName: String): String = if (binaryDataLibraryVersion.endsWith("-SNAPSHOTX")) ":${libName}-${binaryDataLibraryVersion}" else "org.exbin.auxiliary:${libName}:${binaryDataLibraryVersion}"

repositories {
    flatDir {
        dirs("lib", "lib/jetbrains", "plugins")
    }
    mavenLocal()
}

dependencies {
    implementation(jaguifLibrary("jaguif-core"))
    implementation(jaguifLibrary("jaguif-basic"))
    implementation(jaguifLibrary("jaguif-addon"))
    implementation(jaguifLibrary("jaguif-ui"))
    implementation(jaguifLibrary("jaguif-ui-api"))
    implementation(jaguifLibrary("jaguif-ui-theme"))
    implementation(jaguifLibrary("jaguif-ui-theme-api"))
    implementation(jaguifLibrary("jaguif-contribution"))
    implementation(jaguifLibrary("jaguif-contribution-api"))
    implementation(jaguifLibrary("jaguif-context"))
    implementation(jaguifLibrary("jaguif-context-api"))
    implementation(jaguifLibrary("jaguif-component"))
    implementation(jaguifLibrary("jaguif-component-api"))
    implementation(jaguifLibrary("jaguif-frame"))
    implementation(jaguifLibrary("jaguif-frame-api"))
//    implementation(jaguifLibrary("jaguif-data"))
    implementation(jaguifLibrary("jaguif-window"))
    implementation(jaguifLibrary("jaguif-window-api"))
    implementation(jaguifLibrary("jaguif-action"))
    implementation(jaguifLibrary("jaguif-action-api"))
    implementation(jaguifLibrary("jaguif-file"))
    implementation(jaguifLibrary("jaguif-file-api"))
    implementation(jaguifLibrary("jaguif-document"))
    implementation(jaguifLibrary("jaguif-document-api"))
    implementation(jaguifLibrary("jaguif-docking"))
    implementation(jaguifLibrary("jaguif-docking-api"))
    implementation(jaguifLibrary("jaguif-docking-multi"))
    implementation(jaguifLibrary("jaguif-docking-multi-api"))
    implementation(jaguifLibrary("jaguif-about"))
    implementation(jaguifLibrary("jaguif-about-api"))
    implementation(jaguifLibrary("jaguif-operation"))
    implementation(jaguifLibrary("jaguif-operation-api"))
    implementation(jaguifLibrary("jaguif-operation-undo"))
    implementation(jaguifLibrary("jaguif-operation-undo-api"))
    implementation(jaguifLibrary("jaguif-menu"))
    implementation(jaguifLibrary("jaguif-menu-api"))
    implementation(jaguifLibrary("jaguif-menu-popup"))
    implementation(jaguifLibrary("jaguif-menu-popup-api"))
    implementation(jaguifLibrary("jaguif-toolbar"))
    implementation(jaguifLibrary("jaguif-toolbar-api"))
    implementation(jaguifLibrary("jaguif-statusbar"))
    implementation(jaguifLibrary("jaguif-statusbar-api"))
    implementation(jaguifLibrary("jaguif-sidebar"))
    implementation(jaguifLibrary("jaguif-sidebar-api"))
    implementation(jaguifLibrary("jaguif-help"))
    implementation(jaguifLibrary("jaguif-help-api"))
    implementation(jaguifLibrary("jaguif-help-online"))
    implementation(jaguifLibrary("jaguif-help-online-api"))
    implementation(jaguifLibrary("jaguif-options"))
    implementation(jaguifLibrary("jaguif-options-api"))
    implementation(jaguifLibrary("jaguif-options-settings"))
    implementation(jaguifLibrary("jaguif-options-settings-api"))
    implementation(jaguifLibrary("jaguif-language"))
    implementation(jaguifLibrary("jaguif-language-api"))
    implementation(jaguifLibrary("jaguif-text-encoding"))
    implementation(jaguifLibrary("jaguif-text-font"))
    implementation(binedAppLibrary("bined-jaguif-component"))
    implementation(binedAppLibrary("bined-jaguif-viewer"))
    implementation(binedAppLibrary("bined-jaguif-editor"))
    implementation(binedAppLibrary("bined-jaguif-document"))
    implementation(binedAppLibrary("bined-jaguif-theme"))
    implementation(binedAppLibrary("bined-jaguif-search"))
    implementation(binedAppLibrary("bined-jaguif-bookmarks"))
    implementation(binedAppLibrary("bined-jaguif-compare"))
    implementation(binedAppLibrary("bined-jaguif-inspector"))
    implementation(binedAppLibrary("bined-jaguif-macro"))
    implementation(binedAppLibrary("bined-jaguif-objectdata"))
    implementation(binedAppLibrary("bined-jaguif-operation-method"))
    implementation(binedAppLibrary("bined-jaguif-operation-code"))
    implementation(binedAppLibrary("bined-jaguif-operation-bouncycastle"))
    implementation(binedAppLibrary("bined-jaguif-tool-content"))
    implementation(binedLibrary("bined-core"))
    implementation(binedLibrary("bined-section"))
    implementation(binedLibrary("bined-highlight-swing"))
    implementation(binedLibrary("bined-operation"))
    implementation(binedLibrary("bined-operation-swing"))
    implementation(binedLibrary("bined-swing"))
    implementation(binedLibrary("bined-swing-section"))
    implementation(binaryDataLibrary("binary_data"))
    implementation(binaryDataLibrary("binary_data-array"))
    implementation(binaryDataLibrary("binary_data-delta"))
    implementation(":jaguif-language-cs_CZ-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-de_DE-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-es_ES-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-fr_FR-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-it_IT-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-ja_JP-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-ko_KR-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-pl_PL-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-ru_RU-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-zh_Hans-0.3.0-SNAPSHOT")
    implementation(":jaguif-language-zh_Hant-0.3.0-SNAPSHOT")
    implementation(":jaguif-iconset-material-0.3.0-SNAPSHOT")
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
