BinEd - Binary/Hexadecimal Editor - Plugin for IntelliJ Platform
================================================================

Hexadecimal viewer/editor plugin module for IntelliJ platform.

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

Java Development Kit (JDK) version 11 or later is required to build this project.

IntelliJ Idea platform is necessary to build this plugin. See. http://jetbrains.org  

Assign SDK using 2022.2.1 IntelliJ platform / add and use "Other/Plugin" run configuration to execute.

License
-------

Apache License, Version 2.0 - see LICENSE.txt
