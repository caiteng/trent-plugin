import java.io.File
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("java")
//    id("org.jetbrains.kotlin.jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "org.trent"
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public")
    }
}
dependencies {
    implementation("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("org.jsoup:jsoup:1.16.2")

}

// 获取本地配置
val localProperties = Properties().apply {
    val localPropsFile = File(project.rootDir, "local.properties")
    if (localPropsFile.exists()) {
        load(FileInputStream(localPropsFile))
    }
}


intellij {
    type.set("IC")

    // 优先使用项目内置的IDE
    val projectIdeaDir = File(project.rootDir, "idea-runtime")
    if (projectIdeaDir.exists() && projectIdeaDir.isDirectory && File(projectIdeaDir, "bin").exists()) {
        localPath.set(projectIdeaDir.absolutePath)
        println("Using project bundled IntelliJ IDEA: ${projectIdeaDir.absolutePath}")
    } else {
        // 回退到版本指定（会自动下载）
        version.set("2024.2.4")
        println("Will download IntelliJ IDEA Community Edition 2024.2.4")
    }
    
    // 配置JBR路径（如果项目中有）
    val projectJbrDir = File(project.rootDir, "jbr-runtime")
    if (projectJbrDir.exists()) {
        println("Found project JBR directory: ${projectJbrDir.absolutePath}")
        // 注意：具体配置方式取决于插件版本
    }
    
    // 社区版默认包含Java支持，无需额外插件
    plugins.set(emptyList())
}

tasks {
    withType<JavaCompile> {
        // 使用Java 17进行编译以兼容IntelliJ Platform
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.release.set(17)
        options.encoding = "UTF-8"
    }
//    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions.jvmTarget = "17"
//    }

    patchPluginXml {
        sinceBuild.set("242")
        untilBuild.set("9999.*")
    }
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    // 自定义任务：使用当前IDEA运行
    register("runInCurrentIDEA") {
        dependsOn("runIde")
        doFirst {
            println("Running plugin in your current activated IDEA...")
        }
    }
}