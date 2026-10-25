<p align="center">
	<img src = "https://img.shields.io/badge/Supports-Paper%2026.3-%3Cbrightgreen%3E">
	<img src = "https://img.shields.io/badge/-Library-blue">
	<img src = "https://img.shields.io/badge/-Easy%20to%20use-orange">
</p>

<img src="https://imgur.com/xCRKtdh.png" width="120px" align="right"></img>
# DisguiseAPI
DisguiseAPI is an open-source library for changing player skins and nicknames on Paper 26.3.

### Building

DisguiseAPI supports **Paper 26.3 only** and requires **JDK 25**. The Maven reactor contains the API,
26.3 adapter, and plugin modules.

Paperweight prepares the Paper server dependency for Maven. Run this once per development-bundle
version (PowerShell, from the project root):

```powershell
.\build-tools\paperweight\gradlew.bat -p .\build-tools\paperweight -g D:\cache\.gradle "-Dmaven.repo.local=D:\cache\.maven\repository" publishToMavenLocal
```

Then compile, test, and package the plugin with Maven:

```powershell
mvn "-Dmaven.repo.local=D:\cache\.maven\repository" clean verify
```

Deploy `Plugin/target/DisguiseAPI.jar`. This is also Maven's main plugin artifact for `install`.
It contains only the 26.3 adapter and has the Mojang mapping marker. Server classes and test dependencies
are not bundled. No Spigot BuildTools runs or SpecialSource remapping steps are required.
The first dependency-preparation run needs an internet connection; subsequent plugin builds use Maven.

Regression tests cover the single-adapter selection, immutable skin/name updates and restoration,
texture clearing, profile creation, refresh delegation, and version parsing. Visual skin refresh with
connected players still requires a server test.

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
