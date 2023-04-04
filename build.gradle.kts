plugins {
    kotlin("jvm") version "1.8.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    `kotlin-dsl`
    `java-library`
    `maven-publish`
}

group = "love.chihuyu"
version = "0.0.1-SNAPSHOT"
val pluginVersion: String by project.ext

repositories {
    mavenCentral()
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.hirosuke.me/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:$pluginVersion-R0.1-SNAPSHOT")
    implementation("love.chihuyu:ChihuyuLib:0.1.4")
    implementation("dev.jorel:commandapi-core:8.8.0")
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
        relocate("kotlin", "love.chihuyu.$loweredProject.lib.kotlin")
        relocate("love.chihuyu", "love.chihuyu.$loweredProject.lib.love.chihuyu")
        relocate("dev.jorel.commandapi", "love.chihuyu.$loweredProject.lib.dev.jorel.commandapi")
    }

    runServer {
        minecraftVersion("1.19.4")
    }
}

nexusPublishing {
    repositories {
        create("repo") {
            nexusUrl.set(uri("https://repo.hirosuke.me/repository/maven-releases/"))
            snapshotRepositoryUrl.set(uri("https://repo.hirosuke.me/repository/maven-snapshots/"))
        }
    }
}

kotlin {
    jvmToolchain(18)
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