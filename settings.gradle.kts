pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/central")
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "VoxelLink"
include("common")
include("bukkit")
include("folia")
include("velocity")
