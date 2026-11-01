<p align="center">
	<img src = "https://img.shields.io/badge/Supports-Paper%201.21.11%20%26%2026.3-%3Cbrightgreen%3E">
	<img src = "https://img.shields.io/badge/-Library-blue">
	<img src = "https://img.shields.io/badge/-Easy%20to%20use-orange">
</p>

<img src="https://imgur.com/xCRKtdh.png" width="120px" align="right"></img>
# DisguiseAPI
DisguiseAPI is an open-source library for changing player skins and nicknames on Paper 1.21.11 and 26.3.

### Building

DisguiseAPI supports **Paper 1.21.11 and 26.3**. Build with **JDK 25**; the plugin uses Java 21
bytecode so it also loads on Paper 1.21.11 running **Java 21**. Paper 26.3 requires Java 25.
The Maven reactor contains the API, both version adapters, and plugin modules.

Paperweight prepares both Paper server dependencies for Maven. Run these once per development-bundle
version (PowerShell, from the project root; have JDK 21 and JDK 25 available to Gradle):

```powershell
.\build-tools\paperweight\gradlew.bat -p .\build-tools\paperweight -g D:\cache\.gradle "-Dmaven.repo.local=D:\cache\.maven\repository" publishToMavenLocal
.\build-tools\paperweight\gradlew.bat -p .\build-tools\paperweight -g D:\cache\.gradle "-Dmaven.repo.local=D:\cache\.maven\repository" -PpaperVersion=1.21.11-R0.1-SNAPSHOT publishToMavenLocal
```

Then compile, test, and package the plugin against each server version with Maven:

```powershell
mvn "-Dmaven.repo.local=D:\cache\.maven\repository" clean verify
mvn "-Dmaven.repo.local=D:\cache\.maven\repository" "-Ppaper-1.21.11" clean verify
```

The default test runtime is Paper 26.3; the `paper-1.21.11` profile switches the shared API and test
runtime to Paper 1.21.11. Each adapter always compiles against its own server dependency. Use Maven's
`-Djvm="path\to\jdk-21\bin\java.exe"` with the 1.21.11 profile to run tests on Java 21 while building
with JDK 25.

Deploy `Plugin/target/DisguiseAPI.jar`. This is also Maven's main plugin artifact for `install`.
It contains both adapters and has the Mojang mapping marker. Server classes and test dependencies
are not bundled. No Spigot BuildTools runs or SpecialSource remapping steps are required.
The first dependency-preparation run needs an internet connection; subsequent plugin builds use Maven.

Regression tests run against both server dependencies and cover version-specific adapter selection,
unsupported-version rejection, immutable skin/name updates and restoration, texture clearing,
profile creation, refresh delegation, and version parsing. Visual skin refresh with connected players
still requires a server test.

### Dependency

To install this repository, you should follow the next steps:

1. Clone this repository: ``git clone https://github.com/TavernSMP/DisguiseAPI.git``
2. Enter into the directory folder: ``cd DisguiseAPI``
3. Build the project using Maven: ``mvn clean install``

After the project has finished building, you may now use the project in your projects.

#### Maven
```xml
<dependency>
  <groupId>net.pinger.disguise</groupId>
  <artifactId>API</artifactId>
  <version>2.0.0</version>
  <scope>provided</scope> <!-- No need for compiling it within the jar since it is already included within the plugin -->
</dependency>
```

#### Gradle
```gradle
dependencies {
    // No need for compiling it within the jar since it is already included within the plugin
    compileOnly 'net.pinger.disguise:API:2.0.0'
}
```

### Documentation

- <a href = "https://itspinger.github.io/DisguiseAPI/">Javadoc:</a> Documentation for the current release 
- <a href = "https://github.com/itspinger/DisguiseAPI/blob/master/CHANGELOG.md">Changelog:</a> Information about the latest updates
- <a href = "https://github.com/itspinger/DisguiseAPI/wiki">User guide:</a> A well written user guide on how to use this library

### License 

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

### Plugins

Down below plugins implementing this library will be mentioned, along with the download link. If you want your plugin to be
included in this list, write me a message on Pinger#5246 (Discord)

- <a href = "https://www.spigotmc.org/resources/disguise.84079/">DisguisePlus</a>
- <a href = "">Hynick</a>
