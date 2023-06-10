plugins {
    kotlin("jvm") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
    id("xyz.jpenilla.run-paper") version "2.1.0"
    `maven-publish`
    `java-library`
    `kotlin-dsl`
    java
    idea
}

group = "love.chihuyu"
version = "0.0.1-SNAPSHOT"
val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
//    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.hirosuke.me/snapshots/")
    maven("https://repo.purpurmc.org/snapshots")
}

/*
1.7.10~1.8.8: "org.github.paperspigot:paperspigot-api:$pluginVersion-R0.1-SNAPSHOT"
1.9.4~1.16.5: "com.destroystokyo.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT"
1.17~1.19.4: "io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT"
 */

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:$pluginVersion-R0.1-SNAPSHOT")
//    compileOnly("io.papermc.paper:paper-api:$pluginVersion-R0.1-SNAPSHOT")
//    compileOnly("org.spigotmc:spigot-api:$pluginVersion-R0.1-SNAPSHOT")
    implementation("love.chihuyu:ChihuyuUtils:1.0.0-SNAPSHOT")
    implementation("dev.jorel:commandapi-core:9.0.2")
    implementation("dev.jorel:commandapi-kotlin:8.8.0")
    implementation(kotlin("stdlib"))
}

ktlint {
    ignoreFailures.set(true)
    disabledRules.add("no-wildcard-imports")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(sourceSets.main.get().resources.srcDirs) {
            filter(org.apache.tools.ant.filters.ReplaceTokens::class, mapOf("tokens" to mapOf(
                "version" to project.version.toString(),
                "name" to project.name,
                "mainPackage" to "love.chihuyu.${project.name.lowercase()}.${project.name}Plugin"
            )))
            filteringCharset = "UTF-8"
        }
    }

    shadowJar {
        val loweredProject = project.name.lowercase()
        exclude("org/slf4j/**")
        exclude("dev.jorel.commandapi")
        relocate("kotlin", "love.chihuyu.$loweredProject.lib.kotlin")
    }

    runServer {
        minecraftVersion(pluginVersion)
    }
}

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url = uri(
                if (project.version.toString().endsWith("SNAPSHOT"))
                    "https://repo.hirosuke.me/snapshots/"
                else
                    "https://repo.hirosuke.me/releases/"
            )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

kotlin {
    jvmToolchain(17)
}

open class SetupTask : DefaultTask() {

    @TaskAction
    fun action() {
        val projectDir = project.projectDir
        projectDir.resolve("renovate.json").deleteOnExit()
        val srcDir = projectDir.resolve("src/main/kotlin/love/chihuyu/${project.name.lowercase()}").apply(File::mkdirs)
        srcDir.resolve("${project.name}Plugin.kt").writeText(
            """
                package love.chihuyu.${project.name.lowercase()}
                
                import org.bukkit.plugin.java.JavaPlugin

                class ${project.name}Plugin: JavaPlugin() {
                    companion object {
                        lateinit var ${project.name}Plugin: JavaPlugin
                    }
                
                    init {
                        ${project.name}Plugin = this
                    }
                }
            """.trimIndent()
        )
    }
}

task<SetupTask>("setup")
