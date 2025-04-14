import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.util.*

plugins {
    java
    war
}


// Чтение свойств из gradle.properties
val altSourceDir: File = file(property("alt.sourceDir")!!)
val altBuildDir: File = file(property("alt.buildDir")!!)
val replacementsFile: File = file(property("alt.replacementsFile")!!)
val altJarBaseName: String = "${project.name}-${property("alt.jarBaseName")}" // Добавляем project.nameэ
val altJarDestinationDir: File = file(property("alt.jarDestinationDir")!!)
val replaceFileExtensions: List<String> = (property("alt.replaceFileExtensions") as String).split(",")
val envName =property("env.name")!!
val envFile = file(envName)

repositories {
    mavenCentral()
}
tasks.withType<JavaCompile> {
    options.annotationProcessorPath = configurations["annotationProcessor"]
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:7.2.0.202503040940-r")
    }
}
dependencies {
    "annotationProcessor"("org.projectlombok:lombok:1.18.34")
    "providedCompile"("jakarta.platform:jakarta.jakartaee-web-api:9.1.0")
    "providedCompile"("org.projectlombok:lombok:1.18.34")
    "implementation"("org.primefaces:primefaces:13.0.10:jakarta")
    "implementation"("org.hibernate:hibernate-core:6.4.8.Final")
    "implementation"("org.postgresql:postgresql:42.7.4")
    "providedCompile"("javax.servlet:javax.servlet-api:4.0.1")
    "providedCompile"("javax.el:javax.el-api:3.0.0")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.10.2")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    "implementation"("org.apache.ant:ant-jsch:1.10.13")
    "implementation"("com.jcraft:jsch:0.1.55")
    "implementation"("org.eclipse.jgit:org.eclipse.jgit:7.2.0.202503040940-r")
}

tasks.war {
    archiveFileName.set("web-lab3.war")
}

tasks.test {
    useJUnitPlatform()
}


val generateChecksums by tasks.creating {
    doLast {
        ant.withGroovyBuilder {
            "checksum"("fileext" to ".md5", "algorithm" to "MD5", "todir" to "target/checksums") {
                "fileset"(mapOf("dir" to ".", "includes" to "**/*.java", "excludes" to "target/**,.git/**"))
            }
            "checksum"("fileext" to ".sha1", "algorithm" to "SHA-1", "todir" to "target/checksums") {
                "fileset"(mapOf("dir" to ".", "includes" to "**/*.java", "excludes" to "target/**,.git/**"))
            }
        }
    }
}

val runChecksumScript by tasks.creating(Exec::class) {
    // Указываем директорию выполнения
    workingDir = project.projectDir

    // Указываем исполняемый файл и его параметры
    commandLine("sh", "./checksum.sh")
}

tasks.named("generateChecksums") {
    finalizedBy(runChecksumScript)
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.named("javadoc").get().outputs)
}


artifacts {
    add("archives", javadocJar)
}
// Задача для копирования исходников во временную директорию
tasks.register<Copy>("copyAltSources") {
    description = "Copies source files to a temporary directory for alt version"
    group = "Alt"
    from("src/main/java")
    into(altSourceDir)
    from("src/main/resources")
    into(altSourceDir)
}

// Задача для применения замен и переименования файлов
tasks.register("applyReplacements") {
    description = "Applies replacements to copied source files and renames files for changed class names"
    group = "Alt"
    dependsOn("copyAltSources")
    inputs.file(replacementsFile)
    outputs.dir(altSourceDir)

    doLast {
        if (!replacementsFile.exists()) {
            throw GradleException("Replacements file $replacementsFile does not exist")
        }

        // Чтение файла замен
        val replacements = Properties().apply {
            replacementsFile.inputStream().use { load(it) }
        }

        // Мапа для отслеживания переименований файлов
        val fileRenames = mutableMapOf<File, String>()

        // Применение замен к содержимому файлов и сбор информации о переименовании
        altSourceDir.walk().filter { file ->
            replaceFileExtensions.any { ext -> file.name.endsWith(ext.trim()) }
        }.forEach { file ->
            var content = file.readText()
            replacements.forEach { key, value ->
                content = content.replace(key.toString(), value.toString())
                // Если ключ совпадает с именем файла (без .java), запоминаем новое имя
                val fileNameWithoutExt = file.nameWithoutExtension
                if (fileNameWithoutExt == key.toString()) {
                    fileRenames[file] = "${value}.java"
                }
            }
            file.writeText(content)
        }

        // Переименование файлов
        fileRenames.forEach { (file, newName) ->
            val newFile = File(file.parentFile, newName)
            if (file.renameTo(newFile)) {
                logger.info("Renamed file ${file.name} to $newName")
            } else {
                throw GradleException("Failed to rename ${file.name} to $newName")
            }
        }
    }
}

// Конфигурация альтернативного sourceSet
sourceSets {
    create("alt") {
        java {
            srcDir(altSourceDir)
        }
        resources {
            srcDir(altSourceDir)
        }
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
}

// Задача для компиляции альтернативного sourceSet
tasks.register<JavaCompile>("compileAlt") {
    description = "Compiles the alt source set"
    group = "Alt"
    dependsOn("applyReplacements")
    source = sourceSets["alt"].java
    classpath = sourceSets["alt"].compileClasspath
    destinationDirectory.set(file("$altBuildDir/classes"))
}

// Задача для создания JAR-архива
tasks.register<Jar>("altJar") {
    description = "Builds a JAR for the alt version"
    group = "Alt"
    dependsOn("compileAlt")
    archiveBaseName.set(altJarBaseName)
    from(tasks.named<JavaCompile>("compileAlt").get().destinationDirectory)
    destinationDirectory.set(altJarDestinationDir)
}

// Основная задача alt
tasks.register("alt") {
    description = "Creates an alternative version of the program with renamed variables and classes"
    group = "Alt"
    dependsOn("altJar", "build") // Вызываем основную задачу build для стандартного JAR
    doLast {
        logger.lifecycle("Alternative JAR created at ${tasks.named<Jar>("altJar").get().archiveFile.get().asFile}")
    }
}


tasks.register("team") {
    description =
        "Checks out 3 previous commits using git worktree, builds them, and packages resulting jars into a zip"
    group = "Team"

    doLast {
        val repoDir = project.rootDir
        val repository = FileRepositoryBuilder()
            .setGitDir(File(repoDir, ".git"))
            .readEnvironment()
            .findGitDir()
            .build()

        val git = Git(repository)

        val commits = git.log().setMaxCount(4).call().toList()
        if (commits.size < 4) {
            throw GradleException("Недостаточно коммитов: нужно минимум 4 (HEAD + 3 предыдущих)")
        }

        val previousCommits = commits.drop(1).take(3)
        val jarsDir = File(buildDir, "teamJars").apply { mkdirs() }

        previousCommits.forEach { commit ->
            val shortHash = commit.name.take(7)
            val worktreeDir = File(buildDir, "worktree_$shortHash")
            val internalWorktreeDir = File(repoDir, ".git/worktrees/worktree_$shortHash")

            // Удаляем физическую папку worktree, если осталась
            if (worktreeDir.exists()) {
                logger.warn("⚠️ Удаляем физический каталог worktree: $worktreeDir")
                worktreeDir.deleteRecursively()
            }

            // Удаляем .git запись, если осталась
            if (internalWorktreeDir.exists()) {
                logger.warn("⚠️ Удаляем .git/worktrees запись: $internalWorktreeDir")
                internalWorktreeDir.deleteRecursively()
            }

            // Очищаем старые записи
            exec {
                commandLine("git", "worktree", "prune")
            }

            logger.lifecycle("▶ Добавляем worktree для коммита $shortHash")
            exec {
                commandLine("git", "worktree", "add", worktreeDir.absolutePath, commit.name)
            }

            // Копируем gradlew, если нужен
            val gradlew = File(repoDir, "gradlew")
            if (!File(worktreeDir, "gradlew").exists() && gradlew.exists()) {
                gradlew.copyTo(File(worktreeDir, "gradlew"), overwrite = true)
                File(worktreeDir, "gradlew").setExecutable(true)
                File(repoDir, "gradle").copyRecursively(File(worktreeDir, "gradle"), overwrite = true)
            }

            logger.lifecycle("⚙️ Сборка коммита $shortHash")
            val settingsFile = listOf("settings.gradle.kts", "settings.gradle", "build.gradle.kts", "build.gradle")
                .map { File(worktreeDir, it) }
                .firstOrNull { it.exists() }

            if (settingsFile == null) {
                logger.warn("⚠️ Пропускаем коммит $shortHash — нет Gradle-сборки в worktree")
                return@forEach
            }

            exec {
                workingDir = worktreeDir
                commandLine("gradle", "clean", "build", "--no-daemon")
            }

            val builtJar = File(worktreeDir, "build/libs").listFiles()
                ?.firstOrNull { it.extension == "jar" }
                ?: throw GradleException("JAR не найден после сборки коммита $shortHash")

            val renamedJar = File(jarsDir, "${project.name}-$shortHash.jar")
            builtJar.copyTo(renamedJar, overwrite = true)
            logger.lifecycle("✅ Сохранён JAR: ${renamedJar.name}")

            logger.lifecycle("🧹 Удаляем временный worktree $shortHash")
            exec {
                commandLine("git", "worktree", "remove", "--force", worktreeDir.absolutePath)
            }
        }

        val zipFile = File(buildDir, "teamArtifacts.zip")
        ant.withGroovyBuilder {
            "zip"("destfile" to zipFile) {
                "fileset"("dir" to jarsDir)
            }
        }

        logger.lifecycle("🎉 Архив с JAR-файлами создан: ${zipFile.absolutePath}")
    }
}
val buildWarWithEnv by tasks.registering(War::class) {
    group = "build"
    description = "Build WAR with env-specific Java version"

    val suffix = (findProperty("envSuffix") as? String) ?: "default"
    archiveFileName.set("web-lab3-$suffix.war")

}

tasks.register("runEnvBuild") {
    group = "build"
    description = "Runs the buildWithEnvWar.sh script with given env file"

    doLast {

        exec {
            commandLine("bash", "./buildWithEnvWar.sh", envFile)
        }
    }
}
