import io.papermc.paperweight.userdev.internal.setup.UserdevSetupTask

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
}

group = "net.pinger.disguise.build"
version = "26.3.build.223-stable"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(25))

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle(version.toString())
}

val setup = tasks.named<UserdevSetupTask>("paperweightUserdevSetup")

publishing.publications.create<MavenPublication>("server") {
    artifactId = "paper-server"
    artifact(setup.flatMap { it.mappedServerJar }) {
        builtBy(setup)
    }
    pom.withXml {
        val dependencies = asNode().appendNode("dependencies")
        configurations.compileClasspath.get().resolvedConfiguration.resolvedArtifacts
            .filter { it.extension == "jar" }
            .sortedBy { it.moduleVersion.id.toString() }
            .forEach { artifact ->
                val dependency = dependencies.appendNode("dependency")
                dependency.appendNode("groupId", artifact.moduleVersion.id.group)
                dependency.appendNode("artifactId", artifact.name)
                dependency.appendNode("version", artifact.moduleVersion.id.version)
                artifact.classifier?.let { dependency.appendNode("classifier", it) }
                val exclusion = dependency.appendNode("exclusions").appendNode("exclusion")
                exclusion.appendNode("groupId", "*")
                exclusion.appendNode("artifactId", "*")
            }
    }
}
