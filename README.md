BinEd - Binary/Hex Editor - Plugin for IntelliJ Platform
========================================================

Hex viewer/editor plugin module for IntelliJ platform.

Homepage: https://bined.exbin.org/intellij-plugin/  

Published as: https://plugins.jetbrains.com/plugin/9339-bined  

Screenshot
----------

![BinEd-Editor Screenshot](images/intellij-screenshot.png?raw=true)

Usage
-----

  * Use "Open as Binary" action in toolbar of "File/Open" dialog or "Open In/Binary Editor" in project files context menu
  * Use "View as Binary" action in context menu in variables/debug window
  * Associate file extension with Binary File file type in Options/Editor/File Types
  * Use "Byte-to-byte compare" in Compare files dialog
  * Use "Edit as Binary" in column context menu in DB view

Features
--------

  * Visualize data as numerical (hexadecimal) codes and text representation
  * Codes can be also binary, octal or decimal
  * Support for Unicode, UTF-8 and other charsets
  * Insert and overwrite edit modes
  * Searching for text / hexadecimal code with found matches highlighting
  * Support for undo/redo
  * Support for files with size up to exabytes

Compiling
---------

Java Development Kit (JDK) version 17 or later is required to build this project.

IntelliJ Idea platform is necessary to build this plugin. See. https://jetbrains.org  

Use "buildPlugin" gradle task to build distribution build.

Building is currently broken as it requires dependencies deployed in local Maven repository from various repositories:

You can try to run following commands. Start at parent directory to "bined" repo directory.

    git clone https://github.com/exbin/exbin-auxiliary-java.git
    cd exbin-auxiliary-java
    gradlew build publish
    cd ..
    git clone https://github.com/exbin/bined-lib-java.git
    cd bined-lib-java
    gradlew build publish
    cd ..
    git clone https://github.com/exbin/exbin-framework-java.git
    cd exbin-framework-java
    gradlew build publish
    cd .. 
    git clone https://github.com/exbin/bined.git
    cd bined
    gradlew build publish
    cd .. 

Use "runIde" gradle task to run.

License
-------

Apache License, Version 2.0 - see LICENSE.txt
