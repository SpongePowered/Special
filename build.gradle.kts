import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("org.cadixdev.licenser") version "0.6.1"
}

group = project.group
version = "${project.properties["minecraftVersion"]}-r${project.properties["apiVersion"].toString().split("-")[0]}"

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/") {
        name = "sponge"
    }
}

dependencies {
    spongeRuntime("org.spongepowered:spongevanilla:1.21.1-12.0.0-RC1803:universal")
}

sponge {
    apiVersion("${project.properties["apiVersion"]}")
    license("MIT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0")
    }
    plugin("royale") {
        displayName("${project.properties["name"]}")
        entrypoint("org.spongepowered.royale.Royale")
        description("Battle Royale, now on Sponge!")
        links {
            homepage("https://spongepowered.org")
            source("https://github.com/SpongePowered/Royale")
            issues("https://github.com/SpongePowered/Royale/issues")
        }
        contributor("Spongie") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks {
    jar {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to project.properties["organization"]
            )
        }
    }
}

license {
    properties {
        this["name"] = project.properties["name"]
        this["organization"] = project.properties["organization"]
        this["url"] = project.properties["url"]
    }
    header(project.file("HEADER.txt"))

    include("**/*.java")
    newLine(false)
}


// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}