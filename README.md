# Kotlin Minecraft Plugin
Best purpur plugin template for me.  
※All of settings are tuned for me, probably you unfamiliar write codes at this template.
※Project name supports camel case only.

## Usage
1. Change project name
2. Set minecraft version in `gradle.properties`
3. Run gradle task `setup`
4. You are the godly plugin developer

## Format
You can format codes to very handsome using ktlint.
1. Configure rules in `build.gradle.kts#ktlint`
2. Run gradle task `ktlintFormat`

## Test
You can test plugin in paper with gradle.  
Server data folder generated in `run/`.
1. Run gradle task `runServer`

## Deploy
If you write library using this.  
Do `generateActionsFile` to generate .yml of github actions automatically.