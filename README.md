<p align="center">
	<img src = "https://img.shields.io/badge/Supports-Spigot%201.21.11%20%26%2026.3-%3Cbrightgreen%3E">
	<img src = "https://img.shields.io/badge/-Library-blue">
	<img src = "https://img.shields.io/badge/-Easy%20to%20use-orange">
</p>

<img src="https://imgur.com/xCRKtdh.png" width="120px" align="right"></img>
# DisguiseAPI
DisguiseAPI is an open-source library for changing player skins and nicknames on Spigot/Paper 1.21.11 and 26.3.

### Building

Build with **JDK 25**. The plugin emits Java 21 bytecode for 1.21.11 servers; 26.3 servers require Java 25.
Maven uses standard **Spigot BuildTools** artifacts, with no custom Paper dependency or Gradle preparation.

On this Windows computer, open PowerShell in the project directory and run:

```powershell
.\build.cmd
```

This runs `clean verify` using JDK 25 from `D:\Java JDK\jdk-25.0.4.7-hotspot` and Maven from
`D:\SpigotMC\FreshBuildTools\apache-maven-3.9.6`. It leaves the finished plugin at
`Plugin\target\DisguiseAPI.jar`. To install the API and plugin into local Maven for your other projects:

```powershell
.\build.cmd clean install
```

The local `.mvn/maven.config` selects `D:\cache\.maven\repository`, where BuildTools already installed:

| Module | Dependency |
| --- | --- |
| API | `org.spigotmc:spigot-api:1.21.11-R0.2-SNAPSHOT` and Mojang Authlib |
| 1.21.11 adapter | `org.spigotmc:spigot:1.21.11-R0.2-SNAPSHOT:remapped-mojang` |
| 26.3 adapter | `org.spigotmc:spigot:26.3-R0.1-SNAPSHOT` |

No additional BuildTools run is needed on this computer. For a fresh setup, follow the
[Spigot BuildTools instructions](https://www.spigotmc.org/wiki/buildtools/): build 1.21.11 with Java 21
and `--rev 1.21.11 --remapped`, then 26.3 with Java 25 and `--rev 26.3`. Install into the same local Maven
repository used by this project. The installed 1.21.11 BuildTools revision is **R0.2**, not R0.1.

With JDK 25 and Maven on PATH, the standard command is `mvn clean verify`. In IntelliJ, reload the root
`pom.xml`, select **JDK 25** as the project SDK and Maven runner JRE, and use the repository from
`.mvn/maven.config` (`D:\cache\.maven\repository`). If IntelliJ overrides it, set that path in Maven settings.
The checked-in `build.cmd` contains the paths for this computer; adjust them on another machine.
The Maven repository override in `.mvn/maven.config` is local and ignored by Git.

The default regression tests use Spigot 26.3. To test against Spigot 1.21.11 on Java 21:

```powershell
.\build.cmd "-Pspigot-1.21.11" "-Djvm=D:\Java JDK\jdk-21.0.4.7-hotspot\bin\java.exe" clean verify
```

Both adapters compile against their own server version. SpecialSource remaps only the 1.21.11 adapter
from Mojang names to Spigot names during packaging. The final JAR includes both adapters and excludes
server/test classes. It intentionally has no global Mojang mapping marker: Paper 1.21.11 must remap
the Spigot adapter when loading it. Paper's plugin remapping must remain enabled on that version.

Tests cover adapter selection, unsupported versions, private immutable profile replacement and
restoration, texture clearing, profile creation, NMS refresh packet order and state resynchronization,
and version parsing. Visual skin refresh with connected players still requires a server test.

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
