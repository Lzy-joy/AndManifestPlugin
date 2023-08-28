package com.lzy.manifest.utils

import org.gradle.api.Project

static String createDir(Project project) {
    def fileDirPath = "${project.rootProject.getRootDir().absolutePath}/AndManifest"
    def fileDir = new File(fileDirPath)
    if (fileDir.exists()) {
        return fileDirPath
    } else {
        def mkdir = fileDir.mkdir()
        if (mkdir) {
            return fileDirPath
        }
        return ""
    }
}

static String createFile(String parentPath, String fileName) {
    def destFile = new File("${parentPath}/${fileName}")
    if (!destFile.exists()) {
        def file = destFile.createNewFile()
        if (file) {
            return destFile.absolutePath
        }
        return ""
    }
    return destFile.absolutePath
}