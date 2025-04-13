import org.gradle.internal.impldep.org.eclipse.jgit.api.Git
import org.gradle.internal.impldep.org.eclipse.jgit.lib.Repository
import org.gradle.internal.impldep.org.eclipse.jgit.storage.file.FileRepositoryBuilder
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

repositories {
    mavenCentral()
}
tasks.withType<JavaCompile> {
    options.annotationProcessorPath = configurations["annotationProcessor"]
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
// Задача team
tasks.register<Zip>("team") {
    description = "Fetches the three previous Git revisions, builds them, and packages the JARs into a ZIP archive"
    group = "Team"
    dependsOn("build")

    val tempDir = file("$buildDir/team-revisions")
    val outputDir = file("$buildDir/team-output")
    val zipFileName = "team-revisions.zip"

    doLast {
        // Инициализация Git-репозитория
        val repository: Repository = FileRepositoryBuilder()
            .setGitDir(file("${projectDir}/.git"))
            .build()
        val git = Git(repository)

        // Получение трех предыдущих коммитов
        val commits = git.log()
            .setMaxCount(4) // HEAD + 3 предыдущих
            .call()
            .toList()
            .drop(1) // Пропускаем HEAD
            .take(3) // Берем 3 предыдущих

        if (commits.size < 3) {
            throw GradleException("Not enough commits in the repository. Found ${commits.size}, need 3.")
        }

        // Создание директорий
        tempDir.mkdirs()
        outputDir.mkdirs()

        // Список JAR-файлов для ZIP
        val jarFiles = mutableListOf<File>()

        commits.forEachIndexed { index, commit ->
            val commitId = commit.name.substring(0, 7) // Короткий хэш
            val revisionDir = file("$tempDir/revision-$commitId")
            val revisionBuildDir = file("$revisionDir/build")

            logger.lifecycle("Processing revision ${index + 1}: $commitId")

            // Копирование проекта во временную директорию
            project.copy {
                from(projectDir)
                into(revisionDir)
                exclude(".git", "build", ".gradle")
            }

            // Чек-аут коммита
            git.checkout()
                .setName(commit.name)
                .call()

            // Выполнение сборки
            val buildResult = exec {
                workingDir = revisionDir
                commandLine = listOf("./gradlew", "build", "-p", revisionDir.absolutePath)
                isIgnoreExitValue = true
            }

            if (buildResult.exitValue != 0) {
                throw GradleException("Build failed for revision $commitId")
            }

            // Копирование JAR в outputDir
            val jarFile = file("$revisionBuildDir/libs/${project.name}-${commitId}.jar")
            val outputJar = file("$outputDir/${project.name}-${commitId}.jar")
            project.copy {
                from(fileTree("$revisionBuildDir/libs").matching { include("*.jar") })
                into(outputDir)
                rename { "${project.name}-${commitId}.jar" }
            }

            if (outputJar.exists()) {
                jarFiles.add(outputJar)
                logger.info("Collected JAR: ${outputJar.name}")
            } else {
                throw GradleException("JAR file not found for revision $commitId")
            }
        }

        // Создание ZIP-архива
        from(jarFiles)
        archiveFileName.set(zipFileName)
        destinationDirectory.set(file("${layout.buildDirectory}"))
        logger.lifecycle("Created ZIP archive at ${layout.buildDirectory}/$zipFileName containing ${jarFiles.size} JARs")
    }

    // Очистка после выполнения
    doLast {
        tempDir.deleteRecursively()
    }
}